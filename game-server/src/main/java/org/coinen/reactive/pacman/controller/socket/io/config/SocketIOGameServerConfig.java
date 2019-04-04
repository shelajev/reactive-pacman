package org.coinen.reactive.pacman.controller.socket.io.config;

import com.corundumstudio.socketio.AckMode;
import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import org.coinen.pacman.Location;
import org.coinen.pacman.Nickname;
import org.coinen.reactive.pacman.service.ExtrasService;
import org.coinen.reactive.pacman.service.GameService;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.context.Context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;

@Configuration
public class SocketIOGameServerConfig {

    private static final String LOCATION_FLUX_KEY = "locationFlux";

    static final Logger LOGGER = LoggerFactory.getLogger(SocketIOGameServerConfig.class);
    @Value("${rsocket.metrics-endpoint}")
    String uri;


    @Bean
    public CommandLineRunner socketIOServerRunner(
        ConfigurableApplicationContext context,
        GameService gameService,
        ExtrasService extrasService,
        MapService mapService,
        PlayerService playerService
    ) {
        return (args) -> {
            com.corundumstudio.socketio.Configuration
                configuration = new com.corundumstudio.socketio.Configuration();
            configuration.setTransports(Transport.WEBSOCKET);
            configuration.setHostname("localhost");
            configuration.setPort(5900);
            configuration.setPingTimeout(60);
            configuration.setAckMode(AckMode.MANUAL);

            final SocketIOServer server = new SocketIOServer(configuration);

            BroadcastOperations broadcastOperations = server.getBroadcastOperations();

            playerService
                .players()
                .subscribe(
                    player -> broadcastOperations.sendEvent("players", (Object) player.toByteArray())
                );

            extrasService
                .extras()
                .subscribe(
                    extra -> broadcastOperations.sendEvent("extras", (Object) extra.toByteArray())
                );


            server.addConnectListener(client -> client.sendEvent("setup", (Object) mapService.getMap().toByteArray()));

            server.addDisconnectListener(client ->
                playerService
                    .disconnectPlayer()
                    .subscriberContext(Context.of("uuid", client.getSessionId()))
                    .subscribe()
            );

            server.addEventListener("start", byte[].class, (client, data, ackSender) -> {
                UnicastProcessor<Location> processor = UnicastProcessor.create();

                client.set(LOCATION_FLUX_KEY, processor);
                playerService
                    .locate(
                        processor.subscriberContext(Context.of("uuid", client.getSessionId()))
                    )
                    .subscriberContext(Context.of("uuid", client.getSessionId()))
                    .subscribe();

                gameService.start(Nickname.parseFrom(data))
                           .subscriberContext(Context.of("uuid", client.getSessionId()))
                           .subscribe(config -> ackSender.sendAckData((Object) config.toByteArray()));
            });

            server.addEventListener("locate", byte[].class, (client, data, ackRequest) -> {
                UnicastProcessor<Location> locationFlux =  client.get(LOCATION_FLUX_KEY);
                locationFlux.onNext(Location.parseFrom(data));
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
