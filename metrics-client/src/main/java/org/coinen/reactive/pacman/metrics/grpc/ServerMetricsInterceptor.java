package org.coinen.reactive.pacman.metrics.grpc;

import java.time.Duration;
import java.time.Instant;

import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.step.StepCounter;

public class ServerMetricsInterceptor implements ServerInterceptor {

    private final Timer timer;
    private final Counter counter;

    public ServerMetricsInterceptor(MeterRegistry registry) {
        timer = Timer.builder("grpc.end.to.end.latency")
                     .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                     .register(registry);


        counter = Counter.builder("grpc.server.end.to.end.throughput")
                         .register(registry);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    ) {
        Instant now = Instant.now();
        Instant time = headers.get(Constants.INSTANT_KEY);

        if (time != null) {
            Duration duration = Duration.between(time, now);
            timer.record(duration);
        }

        counter.increment();


        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
            @Override
            public void onMessage(ReqT message) {
                if (message instanceof Message) {
                    UnknownFieldSet fields = ((Message) message).getUnknownFields();

                    if (fields.hasField(9999)) {
                        Instant now = Instant.now();
                        UnknownFieldSet.Field field = fields.getField(9999);
                        long seconds = field.getFixed64List().get(0);
                        int nanos = field.getFixed32List().get(0);
                        Instant time = Instant.ofEpochSecond(seconds, nanos);
                        Duration duration = Duration.between(time, now);

                        timer.record(duration);
                        counter.increment();
                    }
                }
                super.onMessage(message);
            }
        };
    }
}
