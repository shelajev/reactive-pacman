package org.coinen.reactive.pacman.controller.socket.io;

import com.google.protobuf.Empty;
import org.coinen.pacman.Location;
import org.coinen.pacman.Player;
import org.coinen.reactive.pacman.service.PlayerService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PlayerController {

    final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    public Mono<Empty> locate(Flux<Location> messages) {
        return playerService.locate(messages)
            .thenReturn(Empty.getDefaultInstance());
    }

    public Flux<Player> players() {
        return playerService.players()
                            .onBackpressureDrop();
    }
}
