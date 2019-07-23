package org.coinen.reactive.pacman.controller.rsocket;

import java.nio.charset.Charset;
import java.util.UUID;

import com.google.protobuf.Empty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.rpc.frames.Metadata;
import org.coinen.pacman.Extra;
import org.coinen.reactive.pacman.service.ExtrasService;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import org.springframework.stereotype.Controller;

@Controller
public class ExtrasController implements org.coinen.pacman.ExtrasService {
    final ExtrasService extrasService;

    public ExtrasController(ExtrasService service) {
        extrasService = service;
    }

    @Override
    public Flux<Extra> extras(Empty message, ByteBuf metadata) {
        ByteBuf resolvedMetadata = Metadata.getMetadata(metadata);
        Context context = resolvedMetadata != Unpooled.EMPTY_BUFFER
            ? Context.of("uuid", UUID.fromString(resolvedMetadata.toString(Charset.defaultCharset())))
            : Context.empty();

        return extrasService.extras()
                            .onBackpressureBuffer()
                            .subscriberContext(context);
    }
}
