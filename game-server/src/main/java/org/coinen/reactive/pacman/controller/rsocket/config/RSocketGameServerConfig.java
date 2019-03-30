package org.coinen.reactive.pacman.controller.rsocket.config;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Tracer;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.rpc.RSocketRpcService;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import io.rsocket.spring.boot.RSocketReceiverCustomizer;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.coinen.pacman.ExtrasServiceServer;
import org.coinen.pacman.GameServiceServer;
import org.coinen.pacman.PlayerServiceServer;
import org.coinen.pacman.metrics.MetricsSnapshotHandlerClient;
import org.coinen.pacman.metrics.MetricsSnapshotHandlerServer;
import org.coinen.reactive.pacman.controller.rsocket.ExtrasController;
import org.coinen.reactive.pacman.controller.rsocket.GameController;
import org.coinen.reactive.pacman.controller.rsocket.MetricsSnapshotHandlerProxyController;
import org.coinen.reactive.pacman.controller.rsocket.PlayerController;
import org.coinen.reactive.pacman.controller.rsocket.SetupController;
import org.coinen.reactive.pacman.metrics.ReactiveMetricsRegistry;
import org.coinen.reactive.pacman.metrics.rsocket.ServerMetricsAwareRSocket;
import org.coinen.reactive.pacman.service.ExtrasService;
import org.coinen.reactive.pacman.service.GameService;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.retry.Retry;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RSocketGameServerConfig {

    static final Logger LOGGER = LoggerFactory.getLogger(RSocketGameServerConfig.class);
    @Value("${rsocket.metrics-endpoint:ws://localhost:4000}")
    String uri;

    @Bean
    public RSocketReceiverCustomizer enableResumabilityCustomizer(
        @Qualifier("rSocket") MeterRegistry rSocketMeterRegistry
    ) {
        return factory -> factory
//            .addServerPlugin(socket -> new ServerMetricsAwareRSocket(
//                socket,
//                rSocketMeterRegistry
//            ))
            .frameDecoder(PayloadDecoder.ZERO_COPY);
//            .resume()
//            .resumeSessionDuration(Duration.ofMinutes(1))
//            .resumeStore(token -> new InMemoryResumableFramesStore("server", 16384))
//            .resumeStreamTimeout(Duration.ofMinutes(2));
    }

    @Bean
    @Qualifier("rSocket")
    public MeterRegistry reactiveRSocketMeterRegistry(@Qualifier("rSocket") MetricsSnapshotHandlerClient metricsSnapshotHandlerClient) {
        ReactiveMetricsRegistry registry = new ReactiveMetricsRegistry("rsocket.game.server");

        metricsSnapshotHandlerClient
            .streamMetricsSnapshots(
                registry.asFlux()
            )
            .retryWhen(
                Retry.any()
                     .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofMinutes(1))
                     .retryMax(100)
            )
//            );
            .subscribe();

        return registry;
}

    @Bean
    @Qualifier("rSocket")
    public MetricsSnapshotHandlerClient metricsSnapshotHandlerClient() {
//        ConnectingRSocket connectingRSocket = new ConnectingRSocket(

//        );
         RSocket connectingRSocket = RSocketFactory
             .connect()
             .frameDecoder(PayloadDecoder.ZERO_COPY)
             .transport(WebsocketClientTransport.create(URI.create(uri)))
             .start()
//                      .retryBackoff(
//                          Integer.MAX_VALUE,
//                          Duration.ofSeconds(2)
//                      )
             .block();

        return new MetricsSnapshotHandlerClient(connectingRSocket);
    }

    @Bean
    public MetricsSnapshotHandlerServer metricsSnapshotHandlerServer(
        @Qualifier("rSocket") MetricsSnapshotHandlerClient metricsSnapshotHandlerClient
    ) {
        return new MetricsSnapshotHandlerServer(
            new MetricsSnapshotHandlerProxyController(metricsSnapshotHandlerClient),
            Optional.empty(),
            Optional.empty()
        );
    }

    @Bean
    public ExtrasServiceServer extrasServiceServer(
        ExtrasService extrasService,
        @Qualifier("rSocket") MeterRegistry rSocketMeterRegistry,
        Optional<Tracer> tracer
    ) {
        return new ExtrasServiceServer(new ExtrasController(extrasService),
            Optional.of(rSocketMeterRegistry), tracer);
    }

    @Bean
    public GameServiceServer gameServiceServer(
        GameService gameService,
        @Qualifier("rSocket") MeterRegistry rSocketMeterRegistry,
        Optional<Tracer> tracer
    ) {
        return new GameServiceServer(new GameController(gameService), Optional.of(rSocketMeterRegistry), tracer);
    }

    @Bean
    public PlayerServiceServer playerServiceServer(
        PlayerService playerService,
        @Qualifier("rSocket") MeterRegistry rSocketMeterRegistry,
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
