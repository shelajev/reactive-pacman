package org.coinen.reactive.pacman.controller.rsocket.config;

import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Tracer;
import io.rsocket.rpc.RSocketRpcService;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import org.coinen.pacman.ExtrasServiceServer;
import org.coinen.pacman.GameServiceServer;
import org.coinen.pacman.PlayerServiceServer;
import org.coinen.reactive.pacman.controller.rsocket.ExtrasController;
import org.coinen.reactive.pacman.controller.rsocket.GameController;
import org.coinen.reactive.pacman.controller.rsocket.PlayerController;
import org.coinen.reactive.pacman.controller.rsocket.SetupController;
import org.coinen.reactive.pacman.service.ExtrasService;
import org.coinen.reactive.pacman.service.GameService;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RSocketGameServerConfig {

//    @Bean MeterRegistry remoteMeterRegistry() {
//        return
//    }

    @Bean
    public ExtrasController extrasController(ExtrasService extrasService) {
        return new ExtrasController(extrasService);
    }

    @Bean
    public GameController gameController(GameService gameService) {
        return new GameController(gameService);
    }

    @Bean
    public PlayerController playerController(PlayerService playerService) {
        return new PlayerController(playerService);
    }

    @Bean
    public ExtrasServiceServer extrasServiceServer(
        ExtrasController extrasController,
        Optional<MeterRegistry> rSocketMeterRegistry,
        Optional<Tracer> tracer
    ) {
        return new ExtrasServiceServer(extrasController, rSocketMeterRegistry, tracer);
    }

    @Bean
    public GameServiceServer gameServiceServer(
        GameController gameController,
        Optional<MeterRegistry> rSocketMeterRegistry,
        Optional<Tracer> tracer
    ) {
        return new GameServiceServer(gameController, rSocketMeterRegistry, tracer);
    }

    @Bean
    public PlayerServiceServer playerServiceServer(
        PlayerController playerController,
        Optional<MeterRegistry> rSocketMeterRegistry,
        Optional<Tracer> tracer
    ) {
        return new PlayerServiceServer(playerController, rSocketMeterRegistry, tracer);
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
