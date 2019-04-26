package org.coinen.reactive.pacman.metrics.controller.socket.io;

import com.corundumstudio.socketio.AckMode;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import io.micrometer.core.instrument.Meter;
import org.coinen.pacman.metrics.MetricsSnapshot;
import org.coinen.reactive.pacman.metrics.MappingUtils;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import reactor.core.publisher.EmitterProcessor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;

@Configuration
public class SocketIOServerConfig {

    @Bean
    public CommandLineRunner socketIOServerRunner(
        ConfigurableApplicationContext context,
        MetricsService metricsService
    ) {
        return (args) -> {
            com.corundumstudio.socketio.Configuration
                configuration = new com.corundumstudio.socketio.Configuration();
            configuration.setTransports(Transport.WEBSOCKET);
            configuration.setPort(6900);
            configuration.setPingTimeout(60);
            configuration.setAckMode(AckMode.MANUAL);

            final SocketIOServer server = new SocketIOServer(configuration);
            final EmitterProcessor<Meter> processor = EmitterProcessor.create(2048);
            metricsService.metrics(processor).subscribe();
            server.addEventListener("streamMetricsSnapshots", byte[].class,
                (client, data, ackSender) -> {
                    MetricsSnapshot ms = MetricsSnapshot.parseFrom(data);
                    ms.getMetersList()
                      .stream()
//                      .filter(meter -> {
//                          org.coinen.pacman.metrics.Meter.Type type = meter.getId()
//                                                                           .getType();
//                          return type == org.coinen.pacman.metrics.Meter.Type.COUNTER || type == org.coinen.pacman.metrics.Meter.Type.TIMER;
//                      })
                      .map(MappingUtils::mapMeter)
                      .forEach(processor::onNext);
                });

            server.startAsync();

            context.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent || event instanceof ContextStoppedEvent || event instanceof ApplicationFailedEvent) {
                    server.stop();
                }
            });
        };
    }
}
