package org.coinen.reactive.pacman.controller.socket.io;

import java.util.function.Supplier;

import com.corundumstudio.socketio.SocketIOClient;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;

public class SetupController {

    final Supplier<RequestHandlingRSocket> serverRSocketSupplier;
    final MapService                       mapService;
    final PlayerService                    playerService;

    public SetupController(Supplier<RequestHandlingRSocket> requestHandlingRSocketSupplier,
        MapService service,
        PlayerService playerService) {
        serverRSocketSupplier = requestHandlingRSocketSupplier;
        mapService = service;
        this.playerService = playerService;
    }

    public void accept(SocketIOClient client) {
//        final UUID uuid = new UUID(Clock.systemUTC().millis(), ThreadLocalRandom.current().nextLong());

        client.sendEvent("setup", mapService.getMap());
    }
}
