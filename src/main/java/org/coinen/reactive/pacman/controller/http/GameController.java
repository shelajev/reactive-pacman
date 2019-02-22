package org.coinen.reactive.pacman.controller.http;

import org.coinen.pacman.Config;
import org.coinen.pacman.Nickname;
import org.coinen.reactive.pacman.service.GameService;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/http")
public class GameController {

    final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public Mono<Config> start(@RequestBody Nickname nickname) {
        return gameService.start(nickname);
    }
}
