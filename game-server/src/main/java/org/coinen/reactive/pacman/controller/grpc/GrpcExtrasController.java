package org.coinen.reactive.pacman.controller.grpc;

import com.google.protobuf.Empty;
import org.coinen.pacman.Extra;
import org.coinen.pacman.ReactorExtrasServiceGrpc;
import org.coinen.reactive.pacman.service.ExtrasService;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@GRpcService
public class GrpcExtrasController extends ReactorExtrasServiceGrpc.ExtrasServiceImplBase {
    final ExtrasService extrasService;

    public GrpcExtrasController(ExtrasService service) {
        extrasService = service;
    }

    @Override
    public Flux<Extra> extras(Mono<Empty> request) {
        return extrasService.extras()
                            .onBackpressureDrop()
                            .subscriberContext(Context.of("uuid", UUIDHolder.get()));
    }
}
