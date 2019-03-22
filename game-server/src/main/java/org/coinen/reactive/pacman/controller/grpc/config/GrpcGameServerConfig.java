package org.coinen.reactive.pacman.controller.grpc.config;

import java.net.URI;
import java.time.Duration;

import io.grpc.netty.NettyChannelBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import org.coinen.pacman.metrics.ReactorMetricsSnapshotHandlerGrpc;
import org.coinen.reactive.pacman.metrics.ReactiveMetricsRegistry;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcGameServerConfig {

    @Value("${grpc.metrics-endpoint:http://localhost:9095}")
    String url;

    @Bean
    @Qualifier("grpc")
    public MeterRegistry reactiveGrpcMeterRegistry() {
        ReactiveMetricsRegistry registry = new ReactiveMetricsRegistry("http.game.server");

        Mono.fromSupplier(() -> {
                final URI uri = URI.create(url);
                return NettyChannelBuilder
                    .forAddress(uri.getHost(), uri.getPort())
                    .usePlaintext()
                    .build();
            })
            .retryBackoff(Integer.MAX_VALUE, Duration.ofSeconds(2))
            .subscribe(mc -> {
                ReactorMetricsSnapshotHandlerGrpc.ReactorMetricsSnapshotHandlerStub client =
                    ReactorMetricsSnapshotHandlerGrpc.newReactorStub(mc);

                client.streamMetricsSnapshots(registry.asFlux())
                      .retryWhen(
                          Retry.onlyIf(rc -> !mc.isTerminated())
                               .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofMinutes(1))
                               .retryMax(Long.MAX_VALUE)
                      )
                      .subscribe();
            });

        return registry;
    }
}
