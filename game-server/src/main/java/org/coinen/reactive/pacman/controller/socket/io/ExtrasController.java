package org.coinen.reactive.pacman.controller.socket.io;

import org.coinen.pacman.Extra;
import org.coinen.reactive.pacman.service.ExtrasService;
import reactor.core.publisher.Flux;

public class ExtrasController {
    final ExtrasService extrasService;

    public ExtrasController(ExtrasService service) {
        extrasService = service;
    }

    public Flux<Extra> extras() {
        return extrasService.extras()
                            .onBackpressureBuffer();
    }
}
