package org.coinen.reactive.pacman.metrics.controller.grpc;

import com.google.protobuf.Empty;
import io.netty.buffer.ByteBuf;
import org.coinen.pacman.metrics.MetricsSnapshot;
import org.coinen.pacman.metrics.ReactorMetricsSnapshotHandlerGrpc;
import org.coinen.reactive.pacman.metrics.MappingUtils;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@GRpcService
public class GrpcMetricsController extends
                                   ReactorMetricsSnapshotHandlerGrpc.MetricsSnapshotHandlerImplBase {

    final MetricsService metricsService;

    public GrpcMetricsController(MetricsService service) {
        metricsService = service;
    }

    @Override
    public Mono<Empty> streamMetricsSnapshots(Flux<MetricsSnapshot> metricsStream) {
        return metricsStream.concatMap(ms -> Flux.fromStream(ms.getMetersList()
                                                               .stream()
                                                               .map(MappingUtils::mapMeter)))
                            .as(metricsService::metrics)
                            .thenReturn(Empty.getDefaultInstance());
    }

    @Override
    public Mono<Empty> sendMetricsSnapshot(Mono<MetricsSnapshot> messageMono) {
        return messageMono
            .doOnNext(message ->
                metricsService.metrics(Flux.fromStream(message.getMetersList().stream().map(MappingUtils::mapMeter)))
            )
            .thenReturn(Empty.getDefaultInstance());
    }
}
