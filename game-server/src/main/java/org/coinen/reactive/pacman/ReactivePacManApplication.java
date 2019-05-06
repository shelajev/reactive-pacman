package org.coinen.reactive.pacman;

import io.netty.util.ReferenceCounted;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.ByteBufPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReactivePacManApplication {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ReactivePacManApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ReactivePacManApplication.class, args);
    }

}
