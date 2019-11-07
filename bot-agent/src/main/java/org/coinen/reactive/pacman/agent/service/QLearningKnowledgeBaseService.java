package org.coinen.reactive.pacman.agent.service;

import org.coinen.reactive.pacman.agent.model.CaseStudy;
import org.coinen.reactive.pacman.agent.model.Knowledge;
import org.coinen.reactive.pacman.agent.model.Outcome;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static qlearn.Q_learn.*;

public class QLearningKnowledgeBaseService implements KnowledgeBaseService {
    final KnowledgeRepository knowledgeRepository;

    public QLearningKnowledgeBaseService(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    @Override
    public Mono<Void> learn(Flux<Outcome> outcomeFlux) {
        return outcomeFlux
                .switchOnFirst((s, flux) -> {
                    if (s.hasValue()) {
                        Outcome oc = s.get();
                        Knowledge knowledge = new Knowledge(oc.gameState, newCaseStudy());
                        return flux
                                .scan(new History(0, oc, knowledge, oc, knowledge, null), this::gainKnowledge)
                                .filter(h -> h.toStore != null)
                                .map(h -> h.toStore)
                                .transform(knowledgeRepository::educate);
                    }

                    return flux.then();
                })
                .then();
    }

    History gainKnowledge(History history, Outcome outcome) {
        if (normalizedDistance(compareCases(history.currentKnowledge.gameState, outcome.gameState)) > newCaseThreshold) {    // If agent is in a different case than in previous step
            Knowledge knowledge = knowledgeRepository.searchCase(outcome.gameState);    // This search is for CBR/RL algorithms

            if (knowledge == null) {                                // if 'nextState' is not close to any state reached before. We add it to state list
                knowledge = new Knowledge(outcome.gameState, newCaseStudy());
            }

            // Different rewards are commented
            //reward =  (int) (w1*(game.getScore() - prevScore) +  w2*(game.getTotalTime() - prevTime));
            int reward = outcome.gain;
            //reward = game.getTotalTime() - prevTime;
            int totalReward = reward + history.ongoingGain;

            float max = maxQRow(history.currentKnowledge.caseStudy);
            if (outcome.outcomeType == Outcome.Type.KILLED) {
                max = 0;
                totalReward = 0;
                justEaten = false;
            }

            Knowledge previousKnowledge = history.previousKnowledge;
            Outcome previousOutcome = history.previousOutcome;
            float weight = resolveWeightFromCaseStudy(previousKnowledge.caseStudy, previousOutcome.decision.getDirection().index);
            float v = weight + alpha * ((1 - normalizedDistance(compareCases(history.currentOutcome.gameState, outcome.gameState))) * totalReward + gamma * max - weight);

            return new History(0, history.currentOutcome, history.currentKnowledge, outcome, knowledge,
                    new Knowledge(history.previousKnowledge.gameState, updateWeightForCaseStudy(history.previousKnowledge.caseStudy, previousOutcome.decision.getDirection().index, v)));
        }

        return new History(history.ongoingGain + outcome.gain, history.currentOutcome, history.currentKnowledge, outcome, new Knowledge(outcome.gameState, history.currentKnowledge.caseStudy), history.previousKnowledge);
    }

    CaseStudy newCaseStudy() {
        Random random = ThreadLocalRandom.current();

        float f1 = random.nextFloat(), f2 = random.nextFloat(), f3 = random.nextFloat(), f4 = random.nextFloat();

        return new CaseStudy(f1, f2, f3, f4);
    }

    static class History {
        final int ongoingGain;
        final Outcome previousOutcome;
        final Knowledge previousKnowledge;
        final Outcome currentOutcome;
        final Knowledge currentKnowledge;

        final Knowledge toStore;

        History(int ongoingGain, Outcome previousOutcome, Knowledge previousKnowledge, Outcome currentOutcome, Knowledge currentKnowledge, Knowledge toStore) {
            this.ongoingGain = ongoingGain;
            this.previousOutcome = previousOutcome;
            this.previousKnowledge = previousKnowledge;
            this.currentOutcome = currentOutcome;
            this.currentKnowledge = currentKnowledge;
            this.toStore = toStore;
        }
    }
}
