package org.coinen.reactive.pacman.controller.http;

import java.util.UUID;

import org.coinen.pacman.Config;
import org.coinen.pacman.Nickname;
import org.coinen.reactive.pacman.service.GameService;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/http")
public class HttpGameController {

    final GameService gameService;

    public HttpGameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    @CrossOrigin(origins = "*", methods = RequestMethod.POST, allowedHeaders = "*",
        allowCredentials = "true")
    public Mono<Config> start(@RequestBody Nickname nickname, @CookieValue("uuid") String uuid) {
        return gameService.start(nickname).subscriberContext(Context.of("uuid", UUID.fromString(uuid)));
    }
}
