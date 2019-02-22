package org.coinen.reactive.pacman.reactivepacman.controller.rsocket.config;

import java.util.Collection;
import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Tracer;
import io.rsocket.rpc.RSocketRpcService;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import org.coinen.pacman.ExtrasServiceServer;
import org.coinen.pacman.GameServiceServer;
import org.coinen.pacman.PlayerServiceServer;
import org.coinen.reactive.pacman.reactivepacman.controller.rsocket.ExtrasController;
import org.coinen.reactive.pacman.reactivepacman.controller.rsocket.GameController;
import org.coinen.reactive.pacman.reactivepacman.controller.rsocket.PlayerController;
import org.coinen.reactive.pacman.reactivepacman.controller.rsocket.SetupController;
import org.coinen.reactive.pacman.reactivepacman.service.ExtrasService;
import org.coinen.reactive.pacman.reactivepacman.service.GameService;
import org.coinen.reactive.pacman.reactivepacman.service.MapService;
import org.coinen.reactive.pacman.reactivepacman.service.PlayerService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RSocketConfig {

    @Bean
    public ExtrasServiceServer extrasServiceServer(
        ExtrasService extrasService,
        Optional<MeterRegistry> meterRegistry,
        Optional<Tracer> tracer
    ) {
        return new ExtrasServiceServer(new ExtrasController(extrasService), meterRegistry, tracer);
    }

    @Bean
    public GameServiceServer gameServiceServer(
        GameService gameService,
        Optional<MeterRegistry> meterRegistry,
        Optional<Tracer> tracer
    ) {
        return new GameServiceServer(new GameController(gameService), meterRegistry, tracer);
    }

    @Bean
    public PlayerServiceServer playerServiceServer(
        PlayerService playerService,
        Optional<MeterRegistry> meterRegistry,
        Optional<Tracer> tracer
    ) {
        return new PlayerServiceServer(new PlayerController(playerService), meterRegistry, tracer);
    }

    @Bean
    public SetupController setupController(RequestHandlingRSocket socket, MapService mapService) {
        return new SetupController(socket, mapService);
    }

    @Bean
    public RequestHandlingRSocket requestHandlingRSocket(
        RSocketRpcService[] rSocketRpcServices
    ) {
        return new RequestHandlingRSocket(rSocketRpcServices);
    }
}
