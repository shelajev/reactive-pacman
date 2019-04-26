package org.coinen.reactive.pacman.metrics.rsocket;

import java.util.function.Function;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.RSocketProxy;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ClientMetricsAwareRSocket extends RSocketProxy {

    private final Function<Payload, Payload> measured;

    public ClientMetricsAwareRSocket(RSocket source) {
        super(source);
        this.measured = Function.identity();
    }

    public ClientMetricsAwareRSocket(RSocket source, MeterRegistry registry, String prefix) {
        super(source);
        Counter counter = Counter.builder(prefix + ".rsocket.client.end.to.end.throughput")
                                 .register(registry);
        Timer timer = Timer.builder(prefix + ".rsocket.client.end.to.end.latency")
                           .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                           .register(registry);
        this.measured = ReactiveMetrics.measured(timer, counter);
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        return super.fireAndForget(ReactiveMetrics.timed(payload));
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        return super.requestResponse(ReactiveMetrics.timed(payload))
            .map(measured);
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        return super.requestStream(ReactiveMetrics.timed(payload))
            .map(measured);
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return Flux
            .from(payloads)
            .map(ReactiveMetrics::timed)
            .transform(super::requestChannel)
            .map(measured);
    }
}
