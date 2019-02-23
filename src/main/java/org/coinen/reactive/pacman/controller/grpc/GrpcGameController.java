package org.coinen.reactive.pacman.controller.grpc;

import org.coinen.pacman.Config;
import org.coinen.pacman.Nickname;
import org.coinen.pacman.ReactorGameServiceGrpc;
import org.coinen.reactive.pacman.service.GameService;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;

@GRpcService
public class GrpcGameController extends ReactorGameServiceGrpc.GameServiceImplBase {

    final GameService gameService;

    public GrpcGameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Mono<Config> start(Mono<Nickname> message) {
        return message.flatMap(gameService::start);
    }
}
