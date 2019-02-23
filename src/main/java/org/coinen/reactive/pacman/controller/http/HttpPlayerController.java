package org.coinen.reactive.pacman.controller.http;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.coinen.pacman.Location;
import org.coinen.reactive.pacman.service.PlayerService;
import org.jctools.maps.NonBlockingHashMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/http")
public class HttpPlayerController {

    final PlayerService                                   playerService;
    final ConcurrentMap<UUID, UnicastProcessor<Location>> locationDirectProcessors;

    public HttpPlayerController(PlayerService playerService) {
        this.playerService = playerService;
        this.locationDirectProcessors = new NonBlockingHashMap<>();
    }

    @PostMapping("/locate")
    @CrossOrigin(origins = "http://localhost:9000", methods = RequestMethod.POST, allowedHeaders = "*", allowCredentials = "true")
    public void locate(@RequestBody Location location, @CookieValue("uuid") String uuidString) {
        UUID uuid = UUID.fromString(uuidString);

        UnicastProcessor<Location> processor = locationDirectProcessors.computeIfAbsent(uuid, __ -> {
            UnicastProcessor<Location> unicastProcessor =
                UnicastProcessor.create(
                    Queues.<Location>unboundedMultiproducer().get(),
                    () -> locationDirectProcessors.remove(uuid)
                );

            playerService.locate(unicastProcessor)
                         .subscriberContext(Context.of("uuid", uuid))
                         .subscribe();

            return unicastProcessor;
        });

        processor.onNext(location);
    }

    @GetMapping("/players")
    @CrossOrigin(origins = "http://localhost:9000", methods = RequestMethod.GET, allowedHeaders = "*", allowCredentials = "true")
    public Flux<String> players(@CookieValue("uuid") String uuid) {
        return playerService.players()
                            .map(e -> Arrays.toString(e.toByteArray()))
                            .subscriberContext(Context.of("uuid", UUID.fromString(uuid)));
    }
}
