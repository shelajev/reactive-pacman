package org.coinen.reactive.pacman.metrics.service.support;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.function.DoubleConsumer;

import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.influx.InfluxMeterRegistry;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import org.reactivestreams.Publisher;
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

    private final ConcurrentHashMap<Subscription, Class<?>> subscriptions = new ConcurrentHashMap<>();
    private final Map<Meter.Id, DoubleConsumer>       consumers     = new HashMap<>();

    private final MonoProcessor<Void> terminateProcessor = MonoProcessor.create();
    private final TopicProcessor<Meter> metersProcessor  =
        TopicProcessor.<Meter>builder().autoCancel(false)
                                       .bufferSize(8192)
                                       .requestTaskExecutor(Executors.newWorkStealingPool())
                                       .waitStrategy(WaitStrategy.yielding())
                                       .executor(Executors.newSingleThreadExecutor())
                                       .share(true)
                                       .build();

    public InfluxMetricsBridgeService(InfluxMeterRegistry registry) {
        this.registry = registry;

        initMetersProcessing();
    }

    @Override
    public Mono<Void> metrics(Publisher<Meter> metersFlux) {
        Flux.from(metersFlux)
            .subscribe(new BaseSubscriber<>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    int size = subscriptions.size() + 1;
                    int requestPerSubscription = (int) Math.ceil(8192 / size);

                    subscription.request(requestPerSubscription);
                    subscriptions.put(subscription, InfluxMetricsBridgeService.class);
                }

                @Override
                protected void hookOnNext(Meter value) {
                    metersProcessor.onNext(value);
                }

                @Override
                protected void hookFinally(SignalType type) {
                    subscriptions.remove(upstream());
                }
            });

        return terminateProcessor;
    }

    @Override
    public void request(long n) {
        final List<Subscription> subscriptions = new ArrayList<>(this.subscriptions.keySet());

        int size = subscriptions.size();

        if (size == 0) {
            return;
        }

        int requestPerSubscription = (int) Math.ceil(n / size);

        for (Subscription s: subscriptions) {
            s.request(requestPerSubscription);
        }
    }

    @Override
    public void cancel() { }

    private void initMetersProcessing() {
        metersProcessor.onSubscribe(this);
        metersProcessor
            .subscribe(
                this::record, terminateProcessor::onError, terminateProcessor::onComplete
            );
    }

    private void record(Meter meter) {
        try {
            Meter.Id id = meter.getId();
            Iterable<Tag> tags = id.getTagsAsIterable();
            Meter.Type type = id.getType();
            String description = id.getDescription();

            for (Measurement measurement: meter.measure()) {
                switch (type) {
                    case GAUGE:
                        consumers
                            .computeIfAbsent(
                                id,
                                i -> {
                                    AtomicDouble holder = new AtomicDouble();
                                    registry.gauge(generateInfluxDbFriendNames(i), tags, holder);
                                    return holder::set;
                                })
                            .accept(measurement.getValue());
                        break;
                    case LONG_TASK_TIMER:
                    case TIMER:
                        consumers
                            .computeIfAbsent(
                                id,
                                i ->
                                    new DoubleConsumer() {
                                        Timer timer = registry.timer(generateInfluxDbFriendNames(i), tags);

                                        @Override
                                        public void accept(double value) {
                                            timer.record(Duration.ofNanos((long) value));
                                        }
                                    })
                            .accept(measurement.getValue());
                        break;
                    case COUNTER:
                        consumers
                            .computeIfAbsent(
                                id,
                                i ->
                                    new DoubleConsumer() {
                                        Counter counter = registry.counter(generateInfluxDbFriendNames(i), tags);

                                        @Override
                                        public void accept(double value) {
                                            counter.increment(value);
                                        }
                                    })
                            .accept(measurement.getValue());
                        break;
                    case DISTRIBUTION_SUMMARY:
                        consumers
                            .computeIfAbsent(
                                id,
                                i ->
                                    new DoubleConsumer() {

                                        DistributionSummary counter =
                                            DistributionSummary.builder(generateInfluxDbFriendNames(i))
                                                               .tags(i.getTags())
                                                               .baseUnit(i.getBaseUnit())
                                                               .description(description)
                                                               .register(registry);

                                        @Override
                                        public void accept(double value) {
                                            counter.record(value);
                                        }
                                    })
                            .accept(measurement.getValue());
                        break;
                    default:
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("error recording metric for " + meter.getId().getName(), t);
        }
    }

    private String generateInfluxDbFriendNames(Meter.Id id) {
        return id.getName();
    }
}
