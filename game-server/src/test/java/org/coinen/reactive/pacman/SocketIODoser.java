package org.coinen.reactive.pacman;

import java.time.Duration;

import com.corundumstudio.socketio.transport.WebSocketTransport;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.coinen.pacman.metrics.MetricsSnapshot;
import org.coinen.reactive.pacman.metrics.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SocketIODoser {

    static final Logger LOGGER = LoggerFactory.getLogger(SocketIODoser.class);

    public static void main(String[] args) throws InterruptedException {

        Hooks.onOperatorDebug();
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

        Flux.range(0, 5)
            .doOnNext(i -> LOGGER.info("Connecting client number: {}", i))
            .concatMap(__ -> Mono
                .fromCallable(() -> {
                    IO.Options options = new IO.Options();
                    options.transports = new String[] {WebSocketTransport.NAME};
                    return IO.socket("http://localhost:5900", options);
                })
                .doOnError(t -> LOGGER.error("Reconnecting. ", t))
                .retryBackoff(10, Duration.ofSeconds(2), Duration.ofSeconds(5))
                .delaySubscription(Duration.ofMillis(1000)),
                1
            )
            .subscribe(socket -> {
                socket.on(Socket.EVENT_CONNECT, (__) -> {
                    metricsSnapshotFlux.hide()
                                       .limitRate(256)
                                       .doOnNext(metricsSnapshot -> {
                                           socket.emit("streamMetricsSnapshots",
                                               (Object) metricsSnapshot.toByteArray());

                                       })
                                       .doOnError(t -> LOGGER.error("Got Error", t))
                                       .subscribeOn(Schedulers.elastic())
                                       .retryBackoff(1000, Duration.ofSeconds(1))
                                       .subscribe();
                })
                .on(Socket.EVENT_CONNECT_ERROR, (__) -> {
                    LOGGER.info("Gotta Connection Error");
                })
                .on(Socket.EVENT_ERROR, (__) -> {
                    LOGGER.info("Gotta Error");
                })
                .on(Socket.EVENT_RECONNECT, (__) -> {
                    LOGGER.info("Gotta Reconnection Error");
                })
                .on(Socket.EVENT_RECONNECTING, (__) -> {
                    LOGGER.info("Reconnecting...");
                });

                socket.connect();
            });


        Thread.sleep(10000000);

    }
}