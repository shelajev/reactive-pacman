package org.coinen.reactive.pacman.metrics.service;

import io.micrometer.core.instrument.Meter;
import reactor.core.publisher.Flux;

public interface MetricsService {

    Flux<Long> metrics(Flux<Meter> messages);
}
