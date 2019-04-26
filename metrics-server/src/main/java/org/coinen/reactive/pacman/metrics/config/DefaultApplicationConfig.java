package org.coinen.reactive.pacman.metrics.config;

import io.micrometer.influx.InfluxMeterRegistry;
import org.coinen.reactive.pacman.metrics.service.support.FastInfluxMetricsBridgeService;
import org.coinen.reactive.pacman.metrics.service.support.UnlimitedInfluxMetricsBridgeService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DefaultApplicationConfig {

    @Bean
    @Primary
    public FastInfluxMetricsBridgeService influxMetricsBridgeService(
        InfluxMeterRegistry meterRegistry
    ) {
        return new FastInfluxMetricsBridgeService(meterRegistry);
    }

    @Bean
    @Qualifier("vip")
    public UnlimitedInfluxMetricsBridgeService unlimitedInfluxMetricsBridgeService(
        InfluxMeterRegistry meterRegistry
    ) {
        return new UnlimitedInfluxMetricsBridgeService(meterRegistry);
    }
}
