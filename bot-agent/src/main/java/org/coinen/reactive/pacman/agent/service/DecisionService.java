package org.coinen.reactive.pacman.agent.service;

import org.coinen.reactive.pacman.agent.model.Decision;
import reactor.core.publisher.Flux;

public interface DecisionService {

    Flux<Decision> decide();
}
