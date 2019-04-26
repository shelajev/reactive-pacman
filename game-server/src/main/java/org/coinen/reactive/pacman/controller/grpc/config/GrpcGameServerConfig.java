package org.coinen.reactive.pacman.controller.grpc.config;

import java.net.URI;
import java.time.Duration;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import org.coinen.pacman.metrics.ReactorMetricsSnapshotHandlerGrpc;
import org.coinen.reactive.pacman.metrics.ReactiveMetricsRegistry;
import org.coinen.reactive.pacman.metrics.grpc.ClientMetricsInterceptor;
import org.coinen.reactive.pacman.metrics.grpc.ServerMetricsInterceptor;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcGameServerConfig {

    static final Logger LOGGER = LoggerFactory.getLogger(GrpcGameServerConfig.class);

    @Value("${grpc.metrics-endpoint}")
    String url;

    @Bean
    @Qualifier("grpc")
    public MeterRegistry reactiveGrpcMeterRegistry(
        @Qualifier("grpc") ReactorMetricsSnapshotHandlerGrpc.ReactorMetricsSnapshotHandlerStub client
    ) {
        ReactiveMetricsRegistry registry = new ReactiveMetricsRegistry("grpc.game.server");

//        client.streamMetricsSnapshots(registry.asFlux())
//              .subscribe();

        return registry;
    }

    @Bean
    @Qualifier("grpc")
    public ReactorMetricsSnapshotHandlerGrpc.ReactorMetricsSnapshotHandlerStub reactorMetricsSnapshotHandlerStub() {
       final ManagedChannel managedChannel = Mono
            .fromSupplier(() -> {
                final URI uri = URI.create(url);
                return NettyChannelBuilder.forAddress(uri.getHost(), uri.getPort())
                                          .intercept(new ClientMetricsInterceptor())
                                          .usePlaintext()
                                          .build();
            })
            .retryBackoff(Integer.MAX_VALUE, Duration.ofSeconds(2))
            .block();

        return ReactorMetricsSnapshotHandlerGrpc.newReactorStub(managedChannel);
    }

    @Bean
    @GRpcGlobalInterceptor
    public UUIDInterceptor uuidInterceptor() {
        return new UUIDInterceptor();
    }

    @Bean
    @GRpcGlobalInterceptor
    public ClientMetricsInterceptor clientLatencyInterceptor() {
        return new ClientMetricsInterceptor();
    }

    @Bean
    @GRpcGlobalInterceptor
    public ServerMetricsInterceptor serverLatencyInterceptor(@Qualifier("rSocket") MeterRegistry registry) {
        return new ServerMetricsInterceptor(registry, "game");
    }
}
