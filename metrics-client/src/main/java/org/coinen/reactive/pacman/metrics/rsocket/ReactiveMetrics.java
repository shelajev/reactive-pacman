package org.coinen.reactive.pacman.metrics.rsocket;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.Payload;
import io.rsocket.util.ByteBufPayload;

abstract class ReactiveMetrics {

    private ReactiveMetrics() {}

    static Payload timed(Payload p) {
        try {
            return ByteBufPayload.create(p.data().retain(),
                TimestampMetadata.encode(ByteBufAllocator.DEFAULT, Instant.now(), p.metadata()));
        }
        finally {
            p.release();
        }
    }

    static Function<Payload, Payload> measured(Timer timer, Counter counter) {
        return payload -> {
            ByteBuf metadata = payload.metadata();
            Instant time = TimestampMetadata.time(metadata);

            if (time != null) {
                Instant now = Instant.now();
                Duration duration = Duration.between(time, now);
                timer.record(duration);
                counter.increment();
            }

            return payload;
        };
    }
}
