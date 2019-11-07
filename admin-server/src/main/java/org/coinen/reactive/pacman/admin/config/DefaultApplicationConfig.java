package org.coinen.reactive.pacman.admin.config;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.coinen.pacman.MapServiceServer;
import org.coinen.pacman.PlayerServiceClient;
import org.coinen.reactive.pacman.admin.config.support.ReconnectingRSocket;
import org.coinen.reactive.pacman.admin.service.support.DefaultScoreBoardService;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultApplicationConfig {

    @Value("${game.server.address}")
    URI gameServerAddress;

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
                        .transport(WebsocketClientTransport.create(gameServerAddress))
                        ::start
                )
                .build();

        return new DefaultScoreBoardService(new PlayerServiceClient(reconnectingRSocket));
    }
}
