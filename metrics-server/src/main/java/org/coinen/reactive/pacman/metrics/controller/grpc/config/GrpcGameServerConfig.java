package org.coinen.reactive.pacman.metrics.controller.grpc.config;

import io.micrometer.influx.InfluxMeterRegistry;
import org.coinen.reactive.pacman.metrics.grpc.ClientMetricsInterceptor;
import org.coinen.reactive.pacman.metrics.grpc.ServerMetricsInterceptor;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcGameServerConfig {

    @Bean
    @GRpcGlobalInterceptor
    public ClientMetricsInterceptor clientLatencyInterceptor() {
        return new ClientMetricsInterceptor();
    }

    @Bean
    @GRpcGlobalInterceptor
    public ServerMetricsInterceptor serverLatencyInterceptor(InfluxMeterRegistry registry) {
        return new ServerMetricsInterceptor(registry, "metrics");
    }
}
