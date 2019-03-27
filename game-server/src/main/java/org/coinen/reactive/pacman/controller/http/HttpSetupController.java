package org.coinen.reactive.pacman.controller.http;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import org.coinen.pacman.Map;
import org.coinen.reactive.pacman.service.MapService;

import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;resetTileCheckresetTileC

@RestController
@RequestMapping("/http")
public class HttpSetupController {

    final RequestHandlingRSocket serverRSocket;
    final MapService mapService;

    public HttpSetupController(RequestHandlingRSocket socket, MapService service) {
        serverRSocket = socket;
        mapService = service;
    }

    @GetMapping("/setup")
    @CrossOrigin(origins = "*", methods = RequestMethod.GET, allowedHeaders = "*", allowCredentials = "true")
    public Map setup(ServerWebExchange webExchange) {
        UUID uuid = new UUID(Clock.systemUTC().millis(), ThreadLocalRandom.current().nextLong());

        webExchange.getResponse().addCookie(ResponseCookie.from("uuid", uuid.toString()).build());

        return mapService.getMap();
    }
}
