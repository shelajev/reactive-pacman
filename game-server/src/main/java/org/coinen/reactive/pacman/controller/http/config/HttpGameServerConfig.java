package org.coinen.reactive.pacman.controller.http.config;

import java.time.Duration;

import io.micrometer.core.instrument.MeterRegistry;
import org.coinen.reactive.pacman.metrics.ReactiveMetricsRegistry;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpGameServerConfig {

    @Value("${http.metrics-endpoint}")
    String url;

    @Bean
    @Qualifier("http")
    public MeterRegistry reactiveHttpMeterRegistry() {
        WebClient client = WebClient.create(url);
        ReactiveMetricsRegistry registry = new ReactiveMetricsRegistry("http.game.server");
        registry.asFlux()
                .retryWhen(
                    Retry.any()
                         .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofMinutes(1))
                         .retryMax(Long.MAX_VALUE)
                )
                .subscribe(ms -> Mono.defer(() -> client.post()
                                       .uri("/http/metrics")
                                       .syncBody(ms)
                                       .exchange()
                                       .flatMap(cr -> Mono.empty())
                )
                                       .onErrorResume(__ -> Mono.empty())
                                       .subscribe());

        return registry;
    }
}
