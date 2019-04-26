package org.coinen.reactive.pacman.metrics.controller.rsocket.config;

import java.util.Optional;

import io.micrometer.influx.InfluxMeterRegistry;
import io.rsocket.SocketAcceptor;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.rpc.RSocketRpcService;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import io.rsocket.spring.boot.RSocketReceiverCustomizer;
import org.coinen.pacman.metrics.MetricsSnapshotHandlerServer;
import org.coinen.reactive.pacman.metrics.controller.rsocket.FilteredMetricsController;
import org.coinen.reactive.pacman.metrics.controller.rsocket.MetricsController;
import org.coinen.reactive.pacman.metrics.rsocket.ServerMetricsAwareRSocket;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import org.coinen.reactive.pacman.metrics.service.support.UnlimitedInfluxMetricsBridgeService;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RSocketConfig {

    @Bean
    public RSocketReceiverCustomizer enableResumabilityCustomizer() {
        return factory -> factory
            .frameDecoder(PayloadDecoder.ZERO_COPY);
//            .resume()
//            .resumeSessionDuration(Duration.ofMinutes(1))
//            .resumeStore(token -> new InMemoryResumableFramesStore("server", 16384))
//            .resumeStreamTimeout(Duration.ofMinutes(2));
    }

    @Bean
    public SocketAcceptor metricsSocketAcceptor(
        ObjectProvider<RequestHandlingRSocket> requestHandlingRSocketObjectProvider,
        InfluxMeterRegistry meterRegistry,
        @Qualifier("vip") UnlimitedInfluxMetricsBridgeService unlimitedInfluxMetricsBridgeService
    ) {
        return (setup, sendingSocket) -> setup.getDataUtf8()
                                              .equals("vip")
            ? Mono.fromSupplier(() -> new MetricsSnapshotHandlerServer(
                new MetricsController(unlimitedInfluxMetricsBridgeService),
                Optional.empty(),
                Optional.empty()
            ))
            :
            Mono.fromSupplier(() -> new ServerMetricsAwareRSocket(
                requestHandlingRSocketObjectProvider.getIfAvailable(),
                meterRegistry,
                "metrics"
            ));
    }

    @Bean
    @Scope("prototype")
    public RequestHandlingRSocket requestHandlingRSocket(
        RSocketRpcService[] rSocketRpcServices
    ) {
        return new RequestHandlingRSocket(rSocketRpcServices);
    }

    @Bean
    public MetricsSnapshotHandlerServer metricsSnapshotHandlerServer(
        MetricsService metricsService,
        InfluxMeterRegistry meterRegistry
    ) {
        return new MetricsSnapshotHandlerServer(
            new FilteredMetricsController(metricsService),
            Optional.of(meterRegistry),
            Optional.empty()
        );
    }
}
