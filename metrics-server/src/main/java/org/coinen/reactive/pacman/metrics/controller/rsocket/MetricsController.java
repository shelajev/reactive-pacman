package org.coinen.reactive.pacman.metrics.controller.rsocket;

import com.google.protobuf.Empty;
import io.netty.buffer.ByteBuf;
import org.coinen.pacman.metrics.MetricsSnapshot;
import org.coinen.pacman.metrics.MetricsSnapshotHandler;
import org.coinen.reactive.pacman.metrics.MappingUtils;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MetricsController implements MetricsSnapshotHandler {

    private final MetricsService metricsService;

    public MetricsController(MetricsService service) {
        metricsService = service;
    }

    @Override
    public Mono<Empty> sendMetricsSnapshot(MetricsSnapshot message, ByteBuf metadata) {
        metricsService.metrics(Flux.fromStream(message.getMetersList().stream().map(MappingUtils::mapMeter)));

        return Mono.just(Empty.getDefaultInstance());
    }

    @Override
    public Mono<Empty> streamMetricsSnapshots(Publisher<MetricsSnapshot> publisher, ByteBuf buf) {
        return Flux.from(publisher)
                   .concatMap(ms -> Flux.fromStream(ms.getMetersList().stream().map(MappingUtils::mapMeter)))
                   .as(metricsService::metrics)
                   .thenReturn(Empty.getDefaultInstance());
    }
}
