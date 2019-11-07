package org.coinen.reactive.pacman.agent.repository;

import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.model.Knowledge;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KnowledgeRepository {

    Mono<Void> educate(Flux<Knowledge> outcomeFlux);

    Knowledge leastRecent();

    Knowledge mostRecent();

    Knowledge searchCase(GameState s);
}
