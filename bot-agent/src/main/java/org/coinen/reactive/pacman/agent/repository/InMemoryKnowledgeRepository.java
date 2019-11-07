package org.coinen.reactive.pacman.agent.repository;

import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.model.Knowledge;
import qlearn.Q_learn;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static qlearn.Q_learn.*;

public class InMemoryKnowledgeRepository implements KnowledgeRepository {
    public final List<Knowledge> knowledgeBase = new ArrayList<>();

    @Override
    public Mono<Void> educate(Flux<Knowledge> outcomeFlux) {
        return outcomeFlux.doOnNext(knowledgeBase::add).then();
    }

    public Knowledge leastRecent() {
        if (knowledgeBase.size() > 1) {
            return knowledgeBase.get(knowledgeBase.size() - 2);
        } else {
            return null;
        }
    }

    @Override
    public Knowledge mostRecent() {
        return knowledgeBase.get(knowledgeBase.size() - 1);
    }

    public Knowledge searchCase(GameState s) {
        Knowledge result = null;
        float comp = Q_learn.MAX_STATE_DISTANCE;
        float currBest = Q_learn.MAX_STATE_DISTANCE;

        for (int i = 0; i < knowledgeBase.size(); i++) {
            Knowledge knowledge = knowledgeBase.get(i);
            comp = compareCases(s, knowledge.gameState);
            if (comp < currBest) {
                currBest = comp;
                result = knowledge;
            }
        }


        currBest = normalizedDistance(currBest);

        if (currBest < newCaseThreshold) {
            return result;
        } else {
            return null;
        }
    }
}
