package org.coinen.reactive.pacman.agent.service.impl;

import org.coinen.reactive.pacman.agent.model.*;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import org.coinen.reactive.pacman.agent.repository.TemporaryHistoryRepository;
import org.coinen.reactive.pacman.agent.service.LearningService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static qlearn.Q_learn.*;

public class QLearningLearningService implements LearningService {
    final KnowledgeRepository knowledgeRepository;
    final TemporaryHistoryRepository temporaryHistoryRepository;

    public QLearningLearningService(KnowledgeRepository knowledgeRepository, TemporaryHistoryRepository temporaryHistoryRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.temporaryHistoryRepository = temporaryHistoryRepository;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Flux<Knowledge> learn(Flux<Outcome> outcomeFlux) {
        return outcomeFlux
                .switchOnFirst((s, flux) -> {
                    if (s.hasValue()) {
                        Outcome initialOutcome = s.get(); // no gain so far
                        Knowledge knowledge = findOrCreateKnowledge(initialOutcome);
                        History initialHistory = new History(0, initialOutcome, knowledge, initialOutcome, knowledge, null);
                        temporaryHistoryRepository.update(initialHistory);
                        return flux
                                .scan(initialHistory, this::gainKnowledge)
                                .filter(h -> h.toStore != null)
                                .map(h -> h.toStore);
                    }

                    return (Flux) flux;
                });
    }

    History gainKnowledge(History history, Outcome nextOutcome) {

        if (nextOutcome.decision == Decision.UNCHANGED) {
            nextOutcome = new Outcome(nextOutcome.gameState, nextOutcome.gain, history.currentOutcome.decision, nextOutcome.outcomeType);
        }

        History nextHistory;

        if (normalizedDistance(compareCases(history.currentKnowledge.gameState, nextOutcome.gameState)) > newCaseThreshold) {    // If agent is in a different case than in previous step
            Knowledge nextKnowledge;
            nextKnowledge = findOrCreateKnowledge(nextOutcome);

            // Different rewards are commented
            int reward = nextOutcome.gain;
            int totalReward = reward + history.ongoingGain;

            float max = maxQRow(history.currentKnowledge.caseStudy);
            if (nextOutcome.outcomeType == Outcome.Type.KILLED) {
                max = 0;
                totalReward = 0;
                justEaten = false;
            }

            Knowledge previousKnowledge = history.previousKnowledge;
            Outcome previousOutcome = history.previousOutcome;
            float weight = resolveWeightFromCaseStudy(previousKnowledge.caseStudy, previousOutcome.decision.getDirection().index);
            float v = weight + alpha * ((1 - normalizedDistance(compareCases(history.currentOutcome.gameState, nextOutcome.gameState))) * totalReward + gamma * max - weight);

            nextHistory = new History(
                0,
                history.currentOutcome,
                history.currentKnowledge,
                nextOutcome,
                nextKnowledge,
                new Knowledge(
                    previousKnowledge.uuid,
                    previousKnowledge.gameState,
                    updateWeightForCaseStudy(history.previousKnowledge.caseStudy, previousOutcome.decision.getDirection().index, v),
                    previousKnowledge.usage
                )
            );
        } else {
            nextHistory = new History(
                history.ongoingGain + nextOutcome.gain,
                history.previousOutcome,
                history.previousKnowledge,
                nextOutcome,
                new Knowledge(history.currentKnowledge.uuid, nextOutcome.gameState, history.currentKnowledge.caseStudy, 1),
                null
            );
        }

        temporaryHistoryRepository.update(nextHistory);

        return nextHistory;
    }

    private Knowledge findOrCreateKnowledge(Outcome nextOutcome) {
        Knowledge nextKnowledge;
        Knowledge knowledge = knowledgeRepository.searchCase(nextOutcome.gameState);    // This search is for CBR/RL algorithms

        if (knowledge == null) {// if 'nextState' is not close to any state reached before. We add it to state list
            nextKnowledge = new Knowledge(UUID.randomUUID().toString(), nextOutcome.gameState, newCaseStudy(), 1);
        } else {
            nextKnowledge = new Knowledge(knowledge.uuid, knowledge.gameState, knowledge.caseStudy, knowledge.usage + 1);
        }

        return nextKnowledge;
    }

    CaseStudy newCaseStudy() {
        Random random = ThreadLocalRandom.current();

        float f1 = random.nextFloat(), f2 = random.nextFloat(), f3 = random.nextFloat(), f4 = random.nextFloat();

        return new CaseStudy(f1, f2, f3, f4);
    }

}
