package org.coinen.reactive.pacman.metrics.service.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
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
import reactor.core.publisher.Operators;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

public class FastInfluxMetricsBridgeService implements MetricsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastInfluxMetricsBridgeService.class);


    private final InfluxMeterRegistry registry;

    private final ConcurrentHashMap<MeterBaseSubscriber, RequestAwareSubscription> subscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Meter.Id, DoubleConsumer> consumers = new ConcurrentHashMap<>();

    long capacity  = 1024;
    long threshold = 10;

    public FastInfluxMetricsBridgeService(InfluxMeterRegistry registry) {
        this.registry = registry;

        initMetersProcessing();
    }

    @Override
    public Mono<Void> metrics(Publisher<Meter> metersFlux) {
       return Flux
           .from(metersFlux)
           .transform(Operators.<Meter, Meter>lift((__, s) -> new MeterBaseSubscriber(
               s,
               subscriptions,
               consumers,
               registry)
           ))
           .then();
    }

    public synchronized void configure(Long capacity, Long threshold) {
        if (capacity != null && capacity > 0) {
            this.capacity = capacity;
            this.threshold = Math.min(this.threshold, capacity / 2);
        }

        if (threshold != null && threshold > 0) {
            this.threshold = Math.min(threshold, this.capacity / 2);
        }
    }

    private void initMetersProcessing() {
        Executors.newScheduledThreadPool(1)
                  .scheduleWithFixedDelay(() -> {
                      final long capacity;
                      final long threshold;

                      synchronized (this) {
                          capacity = this.capacity;
                          threshold = this.threshold;
                      }

                      long n = capacity - remaining();

                      if (n <= 0) {
                          return;
                      }

                      LOGGER.info("Request In. n: [{}]", n);
                      final List<RequestAwareSubscription> subscriptions = new ArrayList<>(this.subscriptions.values());

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

                      for (RequestAwareSubscription s : subscriptions) {
                          MeterBaseSubscriber subscriber = (MeterBaseSubscriber) s.delegate;
                          s.tryFlush(threshold, subscriber.received);
                      }

                      LOGGER.info("Request Out. n: [{}]", n);
                  }, 1000, 1500, TimeUnit.MILLISECONDS);
    }

    private long remaining() {
        final Set<Map.Entry<MeterBaseSubscriber, RequestAwareSubscription>> entries = this.subscriptions.entrySet();
        long remining = 0;

        for (Map.Entry<MeterBaseSubscriber, RequestAwareSubscription> entry : entries) {
            remining += entry.getValue().requested - entry.getKey().received;
        }

        return remining;
    }

    private static class MeterBaseSubscriber extends BaseSubscriber<Meter> {

        private final Subscriber<? super Meter> downstream;
        private final ConcurrentMap<MeterBaseSubscriber, RequestAwareSubscription> subscriptions;
        private final Map<Meter.Id, DoubleConsumer> consumers;
        private final MeterRegistry registry;

        long received = 0;

        private MeterBaseSubscriber(
            Subscriber<? super Meter> downstream,
            ConcurrentMap<MeterBaseSubscriber, RequestAwareSubscription> subscriptions,
            Map<Meter.Id, DoubleConsumer> consumers,
            MeterRegistry registry) {
            this.downstream = downstream;
            this.subscriptions = subscriptions;
            this.consumers = consumers;
            this.registry = registry;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            RequestAwareSubscription s = new RequestAwareSubscription(this);
            subscriptions.put(this, s);
            downstream.onSubscribe(new Subscription() {
                @Override
                public void request(long n) { }

                @Override
                public void cancel() {
                    MeterBaseSubscriber.this.dispose();
                }
            });

            LOGGER.info("Subscribed. S: [{}]", this);
            LOGGER.info("Active Subscribers. N: [{}]", subscriptions.size());
        }

        @Override
        protected void hookOnNext(Meter meter) {
            received++;
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

        @Override
        protected void hookFinally(SignalType type) {
            subscriptions.remove(this);

            LOGGER.info("Unsubscribed. S: [{}]", this);
            LOGGER.info("Active Subscribers. N: [{}]", subscriptions.size());
        }
    }
}
