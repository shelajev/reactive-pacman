package org.coinen.reactive.pacman.controller.rsocket;

import java.nio.charset.Charset;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.rpc.frames.Metadata;
import org.coinen.pacman.Config;
import org.coinen.pacman.Nickname;
import org.coinen.reactive.pacman.service.GameService;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class GameController implements org.coinen.pacman.GameService {

    final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Mono<Config> start(Nickname message, ByteBuf metadata) {
        ByteBuf resolvedMetadata = Metadata.getMetadata(metadata);
        Context context = resolvedMetadata != Unpooled.EMPTY_BUFFER
            ? Context.of("uuid", UUID.fromString(resolvedMetadata.toString(Charset.defaultCharset())))
            : Context.empty();
        return gameService.start(message)
                          .subscriberContext(context);
    }

}
