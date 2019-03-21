package org.coinen.reactive.pacman.metrics.service;

import io.micrometer.core.instrument.Meter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface MetricsService {

    Mono<Void> metrics(Publisher<Meter> messages);
}
