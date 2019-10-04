package org.coinen.reactive.pacman.controller.grpc;

import com.google.protobuf.Empty;
import org.coinen.pacman.Player;
import org.coinen.pacman.ReactorPlayerServiceGrpc;
import org.coinen.reactive.pacman.service.PlayerService;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import static org.coinen.reactive.pacman.controller.grpc.config.UUIDInterceptor.CONTEXT_UUID_KEY;

@GRpcService
public class GrpcPlayerController extends ReactorPlayerServiceGrpc.PlayerServiceImplBase {

    final PlayerService playerService;

    public GrpcPlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public Flux<Player> players(Mono<Empty> message) {
        return playerService
            .players()
            .onBackpressureBuffer()
            .subscriberContext(Context.of("uuid", CONTEXT_UUID_KEY.get()));
    }
}
