package org.coinen.reactive.pacman.metrics.service.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleConsumer;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.influx.InfluxMeterRegistry;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

public class UnlimitedInfluxMetricsBridgeService implements MetricsService {

    private final InfluxMeterRegistry registry;
    private final ConcurrentHashMap<Meter.Id, DoubleConsumer> consumers = new ConcurrentHashMap<>();

    public UnlimitedInfluxMetricsBridgeService(InfluxMeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Mono<Void> metrics(Publisher<Meter> metersFlux) {
       return Flux
           .from(metersFlux)
           .transform(Operators.<Meter, Meter>lift((__, s) -> new MeterBaseSubscriber(
               s,
               consumers,
               registry)
           ))
           .then();
    }

    private static class MeterBaseSubscriber extends BaseSubscriber<Meter> {

        private final Subscriber<? super Meter> downstream;
        private final Map<Meter.Id, DoubleConsumer> consumers;
        private final MeterRegistry registry;

        private MeterBaseSubscriber(
            Subscriber<? super Meter> downstream,
            Map<Meter.Id, DoubleConsumer> consumers,
            MeterRegistry registry) {
            this.downstream = downstream;
            this.consumers = consumers;
            this.registry = registry;
        }

        @Override
        protected void hookOnNext(Meter meter) {
            Recorder.record(meter, consumers, registry);
        }

        @Override
        protected void hookOnComplete() {
            downstream.onComplete();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            downstream.onError(throwable);
        }
    }
}
