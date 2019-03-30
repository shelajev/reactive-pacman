package org.coinen.reactive.pacman.metrics.service.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.DoubleConsumer;

import io.micrometer.core.instrument.Meter;
import io.micrometer.influx.InfluxMeterRegistry;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.TopicProcessor;
import reactor.util.concurrent.WaitStrategy;

public class InfluxMetricsBridgeService implements MetricsService, Subscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxMetricsBridgeService.class);

    private final InfluxMeterRegistry registry;

    private final ConcurrentHashMap<Subscriber, RequestAwareSubscription> subscriptions =
        new ConcurrentHashMap<>();
    private final Map<Meter.Id, DoubleConsumer> consumers = new HashMap<>();

    private final MonoProcessor<Void> terminateProcessor = MonoProcessor.create();
    private final TopicProcessor<Meter> metersProcessor  =
        TopicProcessor.<Meter>builder().autoCancel(false)
                                       .bufferSize(8192)
                                       .requestTaskExecutor(Executors.newWorkStealingPool())
                                       .waitStrategy(WaitStrategy.liteBlocking())
                                       .executor(Executors.newSingleThreadExecutor())
                                       .share(true)
                                       .build();

    public InfluxMetricsBridgeService(InfluxMeterRegistry registry) {
        this.registry = registry;

        initMetersProcessing();
    }

    @Override
    public Mono<Void> metrics(Publisher<Meter> metersFlux) {
        BaseSubscriber<Meter> subscriber = new BaseSubscriber<>() {
            long received = 0;
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                int size = subscriptions.size();
                RequestAwareSubscription requestAwareSubscription = new RequestAwareSubscription(subscription);
                subscriptions.put(this, requestAwareSubscription);

                if (size == 0) {
                    requestAwareSubscription.request(metersProcessor.getAvailableCapacity());
                }

                LOGGER.info("Subscribed. S: [{}]", this);
                LOGGER.info("Active Subscribers. N: [{}]", subscriptions.size());
            }

            @Override
            protected void hookOnNext(Meter value) {
                received++;
                metersProcessor.onNext(value);
            }

            @Override
            protected void hookFinally(SignalType type) {
                RequestAwareSubscription subscription = subscriptions.remove(this);
                long requested = subscription.requested;

                if (requested != Long.MAX_VALUE) {
                    InfluxMetricsBridgeService.this.request(requested - received);
                }

                LOGGER.info("Unsubscribed. S: [{}]", this);
                LOGGER.info("Active Subscribers. N: [{}]", subscriptions.size());
            }
        };

        Flux.from(metersFlux)
            .subscribe(subscriber);

        return terminateProcessor.doFinally(__ -> subscriber.dispose());
    }

    @Override
    public void request(long n) {
        try {
            LOGGER.info("Request In. n: [{}]", n);
            final List<Subscription> subscriptions = new ArrayList<>(this.subscriptions.values());

            int size = subscriptions.size();

            if (size == 0) {
                return;
            }

            Collections.shuffle(subscriptions);
            while (n > 0) {
                for (Subscription s : subscriptions) {
                    s.request(1);

                    if (--n <= 0) {
                        break;
                    }
                }
            }
        }
        catch (Throwable t) {
            LOGGER.error("Error Requesting.", t);
        }
        LOGGER.info("Request Out. n: [{}]", n);
    }

    @Override
    public void cancel() { }

    private void initMetersProcessing() {
        final Map<Meter.Id, DoubleConsumer> consumers = this.consumers;
        final InfluxMeterRegistry registry = this.registry;

        metersProcessor.onSubscribe(this);
        metersProcessor
            .doOnCancel(() -> {
                LOGGER.error("Ops something went wrong. Cancelling");
            })
            .subscribe(
                (meter) -> Recorder.record(meter, consumers, registry),
                cause ->  {
                    LOGGER.error("Ops something went wrong", cause);
                    terminateProcessor.onError(cause);
                },
                () -> {
                    LOGGER.error("Ops something went wrong");
                    terminateProcessor.onComplete();
                }
            );
    }
}
