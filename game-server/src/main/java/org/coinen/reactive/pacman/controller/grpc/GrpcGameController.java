package org.coinen.reactive.pacman.controller.grpc;

import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.rpc.metrics.Metrics;
import org.coinen.pacman.Config;
import org.coinen.pacman.Nickname;
import org.coinen.pacman.ReactorGameServiceGrpc;
import org.coinen.reactive.pacman.controller.grpc.config.UUIDHolder;
import org.coinen.reactive.pacman.service.GameService;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@GRpcService
public class GrpcGameController extends ReactorGameServiceGrpc.GameServiceImplBase {

    final GameService gameService;
    final MeterRegistry registry;

    public GrpcGameController(GameService gameService, MeterRegistry registry) {
        this.gameService = gameService;
        this.registry = registry;
    }

    @Override
    public Mono<Config> start(Mono<Nickname> message) {
        return message.flatMap(gameService::start)
                      .transform(Metrics.<Config>timed(registry, "grpc.server", "service", org.coinen.pacman.GameService.SERVICE, "method", org.coinen.pacman.GameService.METHOD_START))
                      .subscriberContext(Context.of("uuid", UUIDHolder.get()));
    }
}
