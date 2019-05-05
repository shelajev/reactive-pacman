package org.coinen.reactive.pacman.controller.rsocket;

import java.nio.charset.Charset;
import java.util.UUID;

import com.google.protobuf.Empty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.rpc.frames.Metadata;
import org.coinen.pacman.Location;
import org.coinen.pacman.Player;
import org.coinen.reactive.pacman.service.PlayerService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class PlayerController implements org.coinen.pacman.PlayerService {

    final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public Mono<Empty> locate(Publisher<Location> messages, ByteBuf metadata) {
        ByteBuf resolvedMetadata = Metadata.getMetadata(metadata);
        Context context = resolvedMetadata != Unpooled.EMPTY_BUFFER
            ? Context.of("uuid", UUID.fromString(resolvedMetadata.toString(Charset.defaultCharset())))
            : Context.empty();
        return playerService.locate(Flux.from(messages))
                            .subscriberContext(context)
                            .thenReturn(Empty.getDefaultInstance());
    }

    @Override
    public Flux<Player> players(Empty message, ByteBuf metadata) {
        ByteBuf resolvedMetadata = Metadata.getMetadata(metadata);
        Context context = resolvedMetadata != Unpooled.EMPTY_BUFFER
            ? Context.of("uuid", UUID.fromString(resolvedMetadata.toString(Charset.defaultCharset())))
            : Context.empty();

        return playerService.players()
                            .subscriberContext(context)
                            .onBackpressureDrop();
    }
}
