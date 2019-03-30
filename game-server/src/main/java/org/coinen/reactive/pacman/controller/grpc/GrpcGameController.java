package org.coinen.reactive.pacman.controller.grpc;

import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.rpc.metrics.Metrics;
import org.coinen.pacman.Config;
import org.coinen.pacman.Nickname;
import org.coinen.pacman.ReactorGameServiceGrpc;
import org.coinen.reactive.pacman.service.GameService;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import org.springframework.beans.factory.annotation.Qualifier;

import static org.coinen.reactive.pacman.controller.grpc.config.UUIDInterceptor.CONTEXT_UUID_KEY;

@GRpcService
public class GrpcGameController extends ReactorGameServiceGrpc.GameServiceImplBase {

    final GameService gameService;
    final MeterRegistry registry;

    public GrpcGameController(GameService gameService,
        @Qualifier("grpc") MeterRegistry registry) {
        this.gameService = gameService;
        this.registry = registry;
    }

    @Override
    public Mono<Config> start(Mono<Nickname> message) {
        return message.flatMap(gameService::start)
                      .subscriberContext(Context.of("uuid", CONTEXT_UUID_KEY.get()));
    }
}
