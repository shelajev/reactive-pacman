package org.coinen.reactive.pacman.controller.socket.io;

import org.coinen.pacman.Config;
import org.coinen.pacman.Nickname;
import org.coinen.reactive.pacman.service.GameService;
import reactor.core.publisher.Mono;

public class GameController {

    final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    public Mono<Config> start(Nickname message) {
        return gameService.start(message);
    }

}
