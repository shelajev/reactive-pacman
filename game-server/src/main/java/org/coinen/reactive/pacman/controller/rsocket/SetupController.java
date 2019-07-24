package org.coinen.reactive.pacman.controller.rsocket;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.netifi.broker.BrokerClient;
import com.netifi.broker.info.BrokerInfoServiceClient;
import com.netifi.broker.info.Destination;
import com.netifi.broker.info.Event;
import com.netifi.broker.info.Group;
import com.netifi.broker.info.Tag;
import com.netifi.broker.rsocket.BrokerSocket;
import com.netifi.common.tags.Tags;
import io.rsocket.rpc.rsocket.RequestHandlingRSocket;
import org.coinen.pacman.MapServiceClient;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;

@Controller
public class SetupController implements ApplicationContextAware {

    final MapService                       mapService;
    final PlayerService                    playerService;

    public SetupController(MapService service, PlayerService playerService) {
        this.mapService = service;
        this.playerService = playerService;
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
                                      .setGroup("demo.netifi.game.client")
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

                  if (event.getType() == Event.Type.LEAVE) {
                      return playerService.disconnectPlayer()
                                          .subscriberContext(Context.of("uuid", uuid));
                  } else {
                      BrokerSocket sendingRSocket = brokerClient.groupServiceSocket(
                          "demo.netifi.game.client",
                          Tags.of(destinationName.getKey(), destinationName.getValue()));
                      return new MapServiceClient(sendingRSocket).setup(mapService.getMap())
                                                                 .then(Mono.empty());
                  }
              })
              .subscribe();

    }
}
