package org.coinen.reactive.pacman.controller.grpc;

import com.google.protobuf.Empty;
import org.coinen.pacman.Map;
import org.coinen.pacman.ReactorSetupServiceGrpc;
import org.coinen.reactive.pacman.service.MapService;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;

@GRpcService
public class GrpcSetupController extends ReactorSetupServiceGrpc.SetupServiceImplBase {

    final MapService mapService;

    public GrpcSetupController(MapService mapService) {
        this.mapService = mapService;
    }

    @Override
    public Mono<Map> get(Mono<Empty> request) {
        return Mono.just(mapService.getMap());
    }

}
