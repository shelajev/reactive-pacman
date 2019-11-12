package org.coinen.reactive.pacman.agent.repository.impl;

import com.google.protobuf.Empty;
import io.netty.buffer.Unpooled;
import org.coinen.pacman.learning.KnowledgeBaseSnapshot;
import org.coinen.pacman.learning.KnowledgeService;
import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.model.Knowledge;
import org.coinen.reactive.pacman.agent.model.utils.MapperUtils;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import qlearn.Q_learn;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static qlearn.Q_learn.*;

public class RemoteKnowledgeRepository implements KnowledgeRepository {
    final Map<String, Knowledge> knowledgeBase = new HashMap<>();
    final KnowledgeService knowledgeService;

    public RemoteKnowledgeRepository(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;

        knowledgeService.retrieveLatest(Empty.getDefaultInstance(), Unpooled.EMPTY_BUFFER)
                .doOnNext(this::updateLocalKnowledgeBase)
                .block();
    }

    @Override
    public int size() {
        return knowledgeBase.size();
    }

    @Override
    public Mono<Void> educate(Flux<Knowledge> outcomeFlux) {
        return outcomeFlux
                .publish(shared -> shared
                    .doOnNext(k -> knowledgeBase.put(k.uuid, k))
                    .map(MapperUtils::mapToProto)
                    .transform(k -> knowledgeService.enrich(k, Unpooled.EMPTY_BUFFER))
                    .takeUntilOther(shared.then())
                    .doOnNext(kb -> System.out.println("KnowledgeBase Size : " + kb.getKnowledgeBaseList().size()))
                    .takeLast(1)
                    .next()
                    .doOnNext(this::updateLocalKnowledgeBase)
                )
                .then();
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

    private void updateLocalKnowledgeBase(KnowledgeBaseSnapshot knowledgeBaseSnapshot) {
        knowledgeBase.clear();
        List<org.coinen.pacman.learning.Knowledge> knowledgeBaseList = knowledgeBaseSnapshot.getKnowledgeBaseList();
        for (org.coinen.pacman.learning.Knowledge knowledge : knowledgeBaseList) {
            Knowledge domain = MapperUtils.mapToModel(knowledge);
            knowledgeBase.put(domain.uuid, domain);
        }
    }
}
