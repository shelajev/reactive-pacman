package org.coinen.reactive.pacman.controller.http;

import java.util.Arrays;

import io.micrometer.core.instrument.MeterRegistry;
import io.rsocket.rpc.metrics.Metrics;
import org.coinen.reactive.pacman.service.ExtrasService;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/http")
public class HttpExtrasController {
    final ExtrasService extrasService;

    public HttpExtrasController(ExtrasService service) {
        extrasService = service;
    }

    @GetMapping("/extras")
    @CrossOrigin(origins = "*", methods = RequestMethod.GET, allowedHeaders = "*", allowCredentials = "true")
    public Flux<String> extras(@CookieValue("uuid") String uuid) {
        return extrasService.extras()
                            .map(e -> Arrays.toString(e.toByteArray()))
                            .onBackpressureBuffer()
                            .subscriberContext(Context.of("uuid", uuid));
    }
}
