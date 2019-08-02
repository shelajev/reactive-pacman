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

import static io.rsocket.util.ByteBufPayload.create;

@SpringBootApplication
public class ReactivePacManApplication {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(ReactivePacManApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ReactivePacManApplication.class, args);


        RSocketFactory
            .receive()
            .acceptor(((setup, sendingSocket) -> {
                LOGGER.info("Received Connection");

                sendingSocket
                        .requestResponse(create("Please. Mine Bitcoins"))
                        .map(payload -> {
                            String dataUtf8 = payload.getDataUtf8();
                            payload.release();
                            return dataUtf8;
                        })
                        .log()
                        .subscribe();

                return Mono.just(new AbstractRSocket() {
                    @Override
                    public Flux<Payload> requestStream(Payload payload) {
                        LOGGER.info("Request Stream {}", payload);
                        return Flux.range(0, 100)
                                   .map(i -> create("Hello " + i));
                    }

                });
            }))
            .transport(WebsocketServerTransport.create(8080))
            .start()
            .flatMap(channel -> {
                LOGGER.info("RSocket Server Started on the port {}", channel.address().getPort());
                return channel.onClose();
            })
            .block();
    }

}
