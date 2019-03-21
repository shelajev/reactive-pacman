package org.coinen.reactive.pacman.metrics.controller.rsocket;

import io.netty.buffer.ByteBuf;
import io.rsocket.rpc.metrics.om.MetricsSnapshot;
import io.rsocket.rpc.metrics.om.MetricsSnapshotHandler;
import io.rsocket.rpc.metrics.om.Skew;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class MetricsController implements MetricsSnapshotHandler {

    private final MetricsService metricsService;

    public MetricsController(MetricsService service) {
        metricsService = service;
    }

    @Override
    public Flux<Skew> streamMetrics(Publisher<MetricsSnapshot> publisher, ByteBuf buf) {
        return Flux.from(publisher)
                   .concatMap(ms -> Flux.fromStream(ms.getMetersList().stream().map(MappingUtils::mapMeter)))
                   .as(metricsService::metrics)
                   .onBackpressureDrop()
                   .map(timestamp -> Skew.newBuilder().setTimestamp(timestamp).build());
    }
}
