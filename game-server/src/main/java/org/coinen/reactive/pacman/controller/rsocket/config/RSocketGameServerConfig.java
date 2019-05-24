package org.coinen.reactive.pacman.controller.rsocket.config;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Tracer;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.rpc.RSocketRpcService;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import io.rsocket.spring.boot.RSocketReceiverCustomizer;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.ByteBufPayload;
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
import org.coinen.reactive.pacman.controller.rsocket.support.ReconnectingRSocket;
import org.coinen.reactive.pacman.metrics.ReactiveMetricsRegistry;
import org.coinen.reactive.pacman.metrics.rsocket.ServerMetricsAwareRSocket;
import org.coinen.reactive.pacman.service.ExtrasService;
import org.coinen.reactive.pacman.service.GameService;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
    @Value("${rsocket.metrics-endpoint}")
    String uri;

    @Bean
    public RSocketReceiverCustomizer enableResumabilityCustomizer(
        @Qualifier("rSocket") MeterRegistry rSocketMeterRegistry
    ) {
        return factory -> factory
            .addServerPlugin(socket -> new ServerMetricsAwareRSocket(
                socket,
                rSocketMeterRegistry,
                "game"
            ))
            .frameDecoder(PayloadDecoder.ZERO_COPY);
    }

    @Bean
    @Qualifier("rSocket")
    public ReactiveMetricsRegistry reactiveRSocketMeterRegistry() {
        return new ReactiveMetricsRegistry("rsocket.game.server");
    }

    @Bean
    @Qualifier("rSocket")
    public MetricsSnapshotHandlerClient metricsSnapshotHandlerClient(
        @Qualifier("rSocket") MeterRegistry rSocketMeterRegistry
    ) {
        ReconnectingRSocket connectingRSocket = new ReconnectingRSocket(
            Mono.defer(
            RSocketFactory.connect()
                          .keepAliveAckTimeout(Duration.ofDays(10000))
                          .frameDecoder(PayloadDecoder.ZERO_COPY)
                          .transport(WebsocketClientTransport.create(URI.create(uri)))
                          ::start),
            Duration.ofMillis(500),
            Duration.ofSeconds(1)
        );

        return new MetricsSnapshotHandlerClient(connectingRSocket, rSocketMeterRegistry);
    }
    @Bean
    @Qualifier("rSocket-VIP")
    public MetricsSnapshotHandlerClient vipMetricsSnapshotHandlerClient(
        @Qualifier("rSocket") ReactiveMetricsRegistry registry
    ) {
        ReconnectingRSocket connectingRSocket = new ReconnectingRSocket(
            Mono.defer(
                RSocketFactory.connect()
                              .keepAliveAckTimeout(Duration.ofDays(10000))
                              .setupPayload(ByteBufPayload.create("vip"))
                              .frameDecoder(PayloadDecoder.ZERO_COPY)
                              .transport(WebsocketClientTransport.create(URI.create(uri)))
                    ::start),
            Duration.ofMillis(500),
            Duration.ofSeconds(1)
        );
        MetricsSnapshotHandlerClient metricsSnapshotHandlerClient = new MetricsSnapshotHandlerClient(connectingRSocket);

        Flux.defer(() -> metricsSnapshotHandlerClient.streamMetricsSnapshots(registry.asFlux()))
            .retryWhen(Retry.any()
                            .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofMinutes(1))
                            .retryMax(100))
            .subscribe();

        return metricsSnapshotHandlerClient;
    }

    @Bean
    public MetricsSnapshotHandlerServer metricsSnapshotHandlerServer(
        @Qualifier("rSocket") MetricsSnapshotHandlerClient metricsSnapshotHandlerClient,
        @Qualifier("rSocket") MeterRegistry rSocketMeterRegistry
    ) {
        return new MetricsSnapshotHandlerServer(
            new MetricsSnapshotHandlerProxyController(metricsSnapshotHandlerClient),
            Optional.of(rSocketMeterRegistry),
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
