package org.coinen.reactive.pacman.controller.http;

import java.util.UUID;

import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.rpc.metrics.Metrics;
import org.coinen.pacman.Config;
import org.coinen.pacman.Nickname;
import org.coinen.reactive.pacman.service.GameService;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import org.springframework.beans.factory.annotation.Qualifier;
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
    final MeterRegistry registry;

    public HttpGameController(GameService gameService,
        @Qualifier("http") MeterRegistry registry) {
        this.gameService = gameService;
        this.registry = registry;
    }

    @PostMapping("/start")
    @CrossOrigin(origins = "*", methods = RequestMethod.POST, allowedHeaders = "*",
        allowCredentials = "true")
    public Mono<Config> start(@RequestBody Nickname nickname, @CookieValue("uuid") String uuid) {
        return gameService.start(nickname)
                          .transform(Metrics.<Config>timed(registry, "http.server", "service", org.coinen.pacman.GameService.SERVICE, "method", org.coinen.pacman.GameService.METHOD_START))
                          .subscriberContext(Context.of("uuid", UUID.fromString(uuid)));
    }
}
