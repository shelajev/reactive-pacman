package org.coinen.reactive.pacman.reactivepacman.controller.rsocket;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import org.coinen.pacman.MapServiceClient;
import org.coinen.reactive.pacman.reactivepacman.controller.rsocket.support.UuidAwareRSocket;
import org.coinen.reactive.pacman.reactivepacman.service.MapService;
import reactor.core.publisher.Mono;

public class SetupController implements SocketAcceptor {

    final RequestHandlingRSocket serverRSocket;
    final MapService mapService;

    public SetupController(RequestHandlingRSocket socket, MapService service) {
        serverRSocket = socket;
        mapService = service;
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
        UUID uuid = new UUID(Clock.systemUTC().millis(), ThreadLocalRandom.current().nextLong());

        return Mono.<RSocket>just(new UuidAwareRSocket(serverRSocket, uuid))
                   .mergeWith(new MapServiceClient(sendingSocket).setup(mapService.getMap()).then(Mono.empty()))
                   .singleOrEmpty();
    }
}
