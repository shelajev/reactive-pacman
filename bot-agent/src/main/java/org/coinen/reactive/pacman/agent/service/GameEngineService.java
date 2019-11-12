package org.coinen.reactive.pacman.agent.service;

import org.coinen.reactive.pacman.agent.model.Decision;
import org.coinen.reactive.pacman.agent.model.Outcome;
import reactor.core.publisher.Flux;

public interface GameEngineService {

    Flux<Outcome> run(Flux<Decision> decisionFlux);
}
