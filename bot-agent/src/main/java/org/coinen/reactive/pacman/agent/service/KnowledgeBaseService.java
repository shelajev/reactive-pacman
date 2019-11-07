package org.coinen.reactive.pacman.agent.service;

import org.coinen.reactive.pacman.agent.model.Outcome;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KnowledgeBaseService {

    Mono<Void> learn(Flux<Outcome> outcomeFlux);
}
