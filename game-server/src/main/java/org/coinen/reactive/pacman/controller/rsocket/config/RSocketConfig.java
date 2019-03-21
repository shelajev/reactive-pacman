package org.coinen.reactive.pacman.controller.rsocket.config;

import java.time.Duration;
import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Tracer;
import io.rsocket.RSocketFactory;
import io.rsocket.rpc.RSocketRpcService;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.coinen.pacman.ExtrasServiceServer;
import org.coinen.pacman.GameServiceServer;
import org.coinen.pacman.PlayerServiceServer;
import org.coinen.pacman.metrics.MetricsSnapshotHandlerClient;
import org.coinen.reactive.pacman.controller.rsocket.ExtrasController;
import org.coinen.reactive.pacman.controller.rsocket.GameController;
import org.coinen.reactive.pacman.controller.rsocket.PlayerController;
import org.coinen.reactive.pacman.controller.rsocket.SetupController;
import org.coinen.reactive.pacman.metrics.ReactiveMetricsRegistry;
import org.coinen.reactive.pacman.service.ExtrasService;
import org.coinen.reactive.pacman.service.GameService;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import reactor.retry.Retry;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RSocketConfig {

    @Bean
    public MeterRegistry reactiveRSocketMeterRegistry() {
        ReactiveMetricsRegistry registry = new ReactiveMetricsRegistry("game.server");
        RSocketFactory.connect()
                      .transport(WebsocketClientTransport.create(4000))
                      .start()
                      .retryBackoff(Integer.MAX_VALUE, Duration.ofSeconds(2))
                      .subscribe(rSocket -> {
                          MetricsSnapshotHandlerClient client =
                              new MetricsSnapshotHandlerClient(rSocket);

                          client.streamMetricsSnapshots(registry.asFlux())
                                .retryWhen(
                                    Retry.onlyIf(rc -> !rSocket.isDisposed())
                                         .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofMinutes(1))
                                         .retryMax(Long.MAX_VALUE)
                                )
                                .subscribe();
                      });

        return registry;
    }

    @Bean
    public ExtrasServiceServer extrasServiceServer(
        ExtrasService extrasService,
        MeterRegistry rSocketMeterRegistry,
        Optional<Tracer> tracer
    ) {
        return new ExtrasServiceServer(new ExtrasController(extrasService),
            Optional.of(rSocketMeterRegistry), tracer);
    }

    @Bean
    public GameServiceServer gameServiceServer(
        GameService gameService,
        MeterRegistry rSocketMeterRegistry,
        Optional<Tracer> tracer
    ) {
        return new GameServiceServer(new GameController(gameService), Optional.of(rSocketMeterRegistry), tracer);
    }

    @Bean
    public PlayerServiceServer playerServiceServer(
        PlayerService playerService,
        MeterRegistry rSocketMeterRegistry,
        Optional<Tracer> tracer
    ) {
        return new PlayerServiceServer(new PlayerController(playerService), Optional.of(rSocketMeterRegistry), tracer);
    }

    @Bean
    public SetupController setupController(
        ObjectProvider<RequestHandlingRSocket> socket,
        MapService mapService,
        PlayerService playerService) {
        return new SetupController(socket::getIfAvailable, mapService, playerService);
    }

    @Bean
    @Scope("prototype")
    public RequestHandlingRSocket requestHandlingRSocket(
        RSocketRpcService[] rSocketRpcServices
    ) {
        return new RequestHandlingRSocket(rSocketRpcServices);
    }
}
