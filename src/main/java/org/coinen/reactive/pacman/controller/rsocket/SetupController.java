package org.coinen.reactive.pacman.controller.rsocket;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import org.coinen.pacman.MapServiceClient;
import org.coinen.reactive.pacman.controller.rsocket.support.UuidAwareRSocket;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import reactor.core.publisher.Mono;

public class SetupController implements SocketAcceptor {

    final RequestHandlingRSocket serverRSocket;
    final MapService mapService;
    final PlayerService playerService;

    public SetupController(RequestHandlingRSocket socket,
        MapService service,
        PlayerService playerService) {
        serverRSocket = socket;
        mapService = service;
        this.playerService = playerService;
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
        UUID uuid = new UUID(Clock.systemUTC().millis(), ThreadLocalRandom.current().nextLong());
        UuidAwareRSocket data = new UuidAwareRSocket(serverRSocket, uuid);

        data.onClose()
            .log()
            .then(playerService.disconnectPlayer())
            .subscribe();
        return Mono.<RSocket>just(data)
                   .mergeWith(new MapServiceClient(sendingSocket).setup(mapService.getMap()).then(Mono.empty()))
                   .singleOrEmpty();
    }
}
