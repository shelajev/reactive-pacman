package org.coinen.reactive.pacman.metrics.config;

import io.micrometer.influx.InfluxMeterRegistry;
import org.coinen.reactive.pacman.metrics.service.support.InfluxMetricsBridgeService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultApplicationConfig {

    @Bean
    public InfluxMetricsBridgeService influxMetricsBridgeService(
        InfluxMeterRegistry meterRegistry
    ) {
        return new InfluxMetricsBridgeService(meterRegistry);
    }
}
