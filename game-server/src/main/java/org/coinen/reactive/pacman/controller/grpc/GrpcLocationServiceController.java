package org.coinen.reactive.pacman.controller.grpc;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import com.google.protobuf.Empty;
import org.coinen.pacman.Location;
import org.coinen.pacman.ReactorLocationServiceGrpc;
import org.coinen.reactive.pacman.service.PlayerService;
import org.jctools.maps.NonBlockingHashMap;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

@GRpcService
public class GrpcLocationServiceController extends ReactorLocationServiceGrpc.LocationServiceImplBase {

    final PlayerService                                   playerService;
    final ConcurrentMap<UUID, UnicastProcessor<Location>> locationDirectProcessors;

    public GrpcLocationServiceController(PlayerService playerService) {
        this.playerService = playerService;
        this.locationDirectProcessors = new NonBlockingHashMap<>();
    }

    @Override
    public Mono<Empty> locate(Mono<Location> messages) {
        UUID uuid = UUIDHolder.get();

        return messages.map(location -> {
            UnicastProcessor<Location> processor =
                locationDirectProcessors.computeIfAbsent(uuid, __ -> {
                    UnicastProcessor<Location> unicastProcessor = UnicastProcessor.create(
                        Queues.<Location>unboundedMultiproducer().get(),
                        () -> locationDirectProcessors.remove(uuid));

                    playerService.locate(unicastProcessor)
                                 .subscriberContext(Context.of("uuid", uuid))
                                 .subscribe();

                    return unicastProcessor;
                });

            processor.onNext(location);

            return Empty.getDefaultInstance();
        });
    }
}
