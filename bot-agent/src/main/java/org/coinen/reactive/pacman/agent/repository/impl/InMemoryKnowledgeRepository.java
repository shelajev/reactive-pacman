package org.coinen.reactive.pacman.agent.repository.impl;

import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.model.Knowledge;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import qlearn.Q_learn;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static qlearn.Q_learn.*;

public class InMemoryKnowledgeRepository implements KnowledgeRepository {
    final Map<String, Knowledge> knowledgeBase = new HashMap<>();

    @Override
    public int size() {
        return knowledgeBase.size();
    }

    @Override
    public Mono<Void> educate(Flux<Knowledge> outcomeFlux) {
        return outcomeFlux.doOnNext(k -> knowledgeBase.put(k.uuid, k)).then();
    }

    public Knowledge searchCase(GameState s) {
        Knowledge result = null;
        float comp;
        float currBest = Q_learn.MAX_STATE_DISTANCE;

        for (Knowledge knowledge : knowledgeBase.values()) {
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
