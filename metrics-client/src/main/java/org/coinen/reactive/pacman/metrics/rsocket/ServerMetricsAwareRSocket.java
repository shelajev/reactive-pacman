package org.coinen.reactive.pacman.metrics.rsocket;

import java.util.function.Function;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.ResponderRSocket;
import io.rsocket.util.RSocketProxy;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ServerMetricsAwareRSocket extends RSocketProxy implements ResponderRSocket {

    private final Function<Payload, Payload> measured;

    public ServerMetricsAwareRSocket(RSocket source, MeterRegistry registry, String prefix) {
        super(source);
        Counter counter = Counter.builder(prefix + ".rsocket.server.end.to.end.throughput")
                                 .register(registry);
        Timer timer = Timer.builder(prefix + ".rsocket.server.end.to.end.latency")
                           .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                           .register(registry);
        this.measured = ReactiveMetrics.measured(timer, counter);
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        return super.fireAndForget(measured.apply(payload));
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        return super.requestResponse(measured.apply(payload))
                    .map(ReactiveMetrics::timed);
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        return super.requestStream(measured.apply(payload))
            .map(ReactiveMetrics::timed);
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return Flux
            .from(payloads)
            .map(measured)
            .transform(super::requestChannel)
            .map(ReactiveMetrics::timed);
    }

    @Override
    public Flux<Payload> requestChannel(Payload payload, Publisher<Payload> payloads) {
        if (source instanceof ResponderRSocket) {
            return ((ResponderRSocket) source)
                .requestChannel(
                    payload,
                    Flux.from(payloads)
                        .map(measured)
                )
                .map(ReactiveMetrics::timed);
        } else {
           return requestChannel(payloads);
        }
    }
}
