package org.coinen.reactive.pacman;

import java.time.Duration;

import io.grpc.netty.NettyChannelBuilder;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.coinen.pacman.metrics.MetricsSnapshot;
import org.coinen.pacman.metrics.ReactorMetricsSnapshotHandlerGrpc;
import org.coinen.reactive.pacman.metrics.MappingUtils;
import org.coinen.reactive.pacman.metrics.grpc.ClientMetricsInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GrpcDoser {

    static final Logger LOGGER = LoggerFactory.getLogger(GrpcDoser.class);

    public static void main(String[] args) throws InterruptedException {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        new JvmMemoryMetrics(Tags.empty()).bindTo(registry);
        new JvmGcMetrics(Tags.empty()).bindTo(registry);
        new JvmThreadMetrics(Tags.empty()).bindTo(registry);
        new ClassLoaderMetrics(Tags.empty()).bindTo(registry);
        new ProcessorMetrics(Tags.empty()).bindTo(registry);
        new UptimeMetrics(Tags.empty()).bindTo(registry);
        new FileDescriptorMetrics(Tags.empty()).bindTo(registry);

        Flux<MetricsSnapshot> metricsSnapshotFlux = Flux
            .generate(sink -> sink.next(
                registry
                    .getMeters()
                    .stream()
                    .reduce(
                        MetricsSnapshot.newBuilder(),
                        (ms, meter) -> ms.addMeters(MappingUtils.mapMeter(meter)),
                        (ms1, ms2) -> ms1.addAllMeters(ms2.getMetersList()))
                    .build()
            ));

        Flux.range(0, 1000)
            .doOnNext(i -> LOGGER.info("Connecting client number: {}", i))
            .concatMap(__ ->
                Mono.fromCallable(() -> NettyChannelBuilder
                        .forAddress("localhost", 9090)
                        .usePlaintext()
                        .intercept(new ClientMetricsInterceptor())
                        .build()
                    )
                    .doOnError(t -> LOGGER.error("Reconnecting. ", t))
                    .retryBackoff(10, Duration.ofSeconds(2), Duration.ofSeconds(5))
                    .delaySubscription(Duration.ofMillis(200)),
                1
            )
            .subscribe(managedChannel -> {
                ReactorMetricsSnapshotHandlerGrpc.ReactorMetricsSnapshotHandlerStub client =
                    ReactorMetricsSnapshotHandlerGrpc.newReactorStub(managedChannel);

                metricsSnapshotFlux
                    .hide()
                    .compose(client::streamMetricsSnapshots)
                    .doOnError(t -> LOGGER.error("Got Error", t))
                    .retryBackoff(1000, Duration.ofSeconds(1))
                    .subscribe();
            });


        Thread.sleep(10000000);

    }
}
