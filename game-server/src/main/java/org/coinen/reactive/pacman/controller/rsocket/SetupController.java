package org.coinen.reactive.pacman.controller.rsocket;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import com.google.protobuf.Empty;
import com.netifi.broker.BrokerClient;
import com.netifi.broker.info.BrokerInfoServiceClient;
import com.netifi.broker.info.Destination;
import com.netifi.broker.info.Event;
import com.netifi.broker.info.Group;
import com.netifi.broker.info.Id;
import com.netifi.broker.info.Tag;
import com.netifi.broker.rsocket.BrokerSocket;
import com.netifi.common.tags.Tags;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import org.coinen.pacman.MapServiceClient;
import org.coinen.reactive.pacman.controller.rsocket.support.UuidAwareRSocket;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SetupController implements SocketAcceptor, ApplicationContextAware {

    final Supplier<RequestHandlingRSocket> serverRSocketSupplier;
    final MapService                       mapService;
    final PlayerService                    playerService;

    public SetupController(Supplier<RequestHandlingRSocket> requestHandlingRSocketSupplier,
        MapService service,
        PlayerService playerService) {
        serverRSocketSupplier = requestHandlingRSocketSupplier;
        mapService = service;
        this.playerService = playerService;
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
        final UUID uuid = new UUID(Clock.systemUTC().millis(), ThreadLocalRandom.current().nextLong());

        sendingSocket.onClose()
                     .onErrorResume(e -> Mono.empty())
                     .then(playerService.disconnectPlayer())
                     .subscriberContext(Context.of("uuid", uuid))
                     .subscribe();

        return Mono.<RSocket>just(new UuidAwareRSocket(serverRSocketSupplier.get(), uuid))
                   .mergeWith(new MapServiceClient(sendingSocket).setup(mapService.getMap())
                                                                 .then(Mono.empty()))
                   .subscriberContext(Context.of("uuid", uuid))
                   .singleOrEmpty();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        BrokerClient brokerClient = applicationContext.getBean(BrokerClient.class);

        BrokerSocket brokerSocket = brokerClient.groupServiceSocket(
            "com.netifi.broker.brokerServices",
            Tags.empty());

        BrokerInfoServiceClient client = new BrokerInfoServiceClient(brokerSocket);

        client.streamGroupEvents(Group.newBuilder()
                                      .setGroup("game-client")
                                      .build())
              .log()
              .concatMap(event -> {
                  Destination destination = event.getDestination();
                  List<Tag> list = destination.getTagsList();
                  Tag destinationName = list.stream()
                                            .filter(tag -> tag.getKey()
                                                              .equals(
                                                                  "com.netifi.destination"))
                                            .findFirst()
                                            .get();
                  UUID uuid = UUID.fromString(destinationName.getValue());

                  if(event.getType() == Event.Type.LEAVE) {
                      return playerService.disconnectPlayer()
                                          .subscriberContext(Context.of("uuid", uuid));
                  } else {
                      BrokerSocket sendingRSocket = brokerClient.groupServiceSocket(
                          "game-client",
                          Tags.of(destinationName.getKey(), destinationName.getValue()));
                      return new MapServiceClient(sendingRSocket).setup(mapService.getMap())
                                                                 .then(Mono.empty());
                  }
              })
              .subscribe();

    }
}
