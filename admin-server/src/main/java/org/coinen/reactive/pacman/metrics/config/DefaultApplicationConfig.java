package org.coinen.reactive.pacman.metrics.config;

import java.time.Duration;
import java.util.Optional;

import io.netty.buffer.ByteBuf;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.coinen.pacman.Map;
import org.coinen.pacman.MapService;
import org.coinen.pacman.MapServiceServer;
import org.coinen.pacman.PlayerServiceClient;
import org.coinen.reactive.pacman.metrics.config.support.ReconnectingRSocket;
import org.coinen.reactive.pacman.metrics.service.support.DefaultScoreBoardService;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultApplicationConfig {

    @Bean
    public DefaultScoreBoardService scoreBoardService(
    ) {
        RSocket reconnectingRSocket =
            ReconnectingRSocket
                .builder()
                .withDefaultRetryOnErrorPredicate()
                .withRetryPeriod(Duration.ofMillis(500), Duration.ofSeconds(2500))
                .withSourceRSocket(
                    RSocketFactory
                        .connect()
                        .acceptor(r ->
                            new RequestHandlingRSocket(
                                new MapServiceServer((message, metadata) -> Mono.empty(), Optional.empty(), Optional.empty(), Optional.empty())
                            )
                        )
                        .transport(WebsocketClientTransport.create("dinoman.netifi.com", 3000))
                        ::start
                )
                .build();

        return new DefaultScoreBoardService(new PlayerServiceClient(reconnectingRSocket));
    }
}
