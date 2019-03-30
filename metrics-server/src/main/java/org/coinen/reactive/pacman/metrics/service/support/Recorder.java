package org.coinen.reactive.pacman.metrics.service.support;

import java.time.Duration;
import java.util.Map;
import java.util.function.DoubleConsumer;

import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Recorder.class);

    public static void record(Meter meter, Map<Meter.Id, DoubleConsumer> consumers, MeterRegistry registry) {
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



    private static String generateInfluxDbFriendNames(Meter.Id id) {
        return id.getName();
    }
}
