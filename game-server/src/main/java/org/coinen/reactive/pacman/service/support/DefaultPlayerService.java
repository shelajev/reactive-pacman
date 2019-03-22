package org.coinen.reactive.pacman.service.support;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.coinen.pacman.Direction;
import org.coinen.pacman.Location;
import org.coinen.pacman.Player;
import org.coinen.pacman.Point;
import org.coinen.reactive.pacman.repository.PlayerRepository;
import org.coinen.reactive.pacman.service.ExtrasService;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class DefaultPlayerService implements PlayerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPlayerService.class);

    final DirectProcessor<Player> playersProcessor = DirectProcessor.create();
    final FluxSink<Player>        playersSink      = playersProcessor.serialize()
                                                                     .sink();

    final PlayerRepository playerRepository;
    final ExtrasService    extrasService;
    final MapService       mapService;

    public DefaultPlayerService(PlayerRepository repository,
        ExtrasService extrasService,
        MapService service) {
        playerRepository = repository;
        this.extrasService = extrasService;
        mapService = service;
        Flux.interval(Duration.ofSeconds(5))
            .doOnNext(this::checkPlayers)
            .subscribe();
    }

    private void checkPlayers(long el) {
        playerRepository.findAll()
            .forEach(p -> {
                if (System.currentTimeMillis() - p.getTimestamp() > 60000) {
                    this.disconnectPlayer()
                        .subscriberContext(Context.of("uuid", UUID.fromString(p.getUuid())))
                        .subscribe();
                }
            });
    }

    @Override
    public Mono<Void> locate(Flux<Location> locationStream) {
        return Mono.subscriberContext()
                   .map(c -> c.<UUID>get("uuid"))
                   .flatMap(uuid -> locationStream
                       .doOnNext(location -> {
                           var time = Instant.now();
                           var updatedPlayer = playerRepository.update(uuid, player -> {
                               Player foundPlayer = playerRepository.findOne(uuid);

                               if (foundPlayer == null) {
                                   return null;
                               }

                               var playerBuilder = foundPlayer.toBuilder()
                                                              .setTimestamp(time.toEpochMilli())
                                                              .setState(Player.State.ACTIVE)
                                                              .setLocation(location);

                               Point position = location.getPosition();

                               List<Player> collisions = playerRepository
                                   .findAll()
                                   .stream()
                                   .filter(p -> p.getState()
                                                 .equals(Player.State.ACTIVE))
                                   .filter(p -> !p.getType()
                                                  .equals(player.getType()))
                                   .filter(p ->
                                       distance(
                                           p.getLocation().getPosition(),
                                           player.getLocation().getPosition()
                                       ) < 100
                                   )
                                   .collect(Collectors.toList());
                               if (collisions.size() > 0) {
                                   if (extrasService.isPowerupActive()
                                       && player.getType()
                                                .equals(Player.Type.GHOST)
                                       || !extrasService.isPowerupActive()
                                       && player.getType()
                                                .equals(Player.Type.PACMAN)) {

                                       playerBuilder.setState(Player.State.DISCONNECTED);
                                       collisions.forEach(collision -> {
                                           Player collidedWith =
                                               playerRepository.update(UUID.fromString(
                                                   collision.getUuid()),
                                                   p -> p.toBuilder()
                                                         .setScore(p.getScore() + 100)
                                                         .build());
                                           playersProcessor.onNext(collidedWith);
                                       });
                                   }
                                   else if (extrasService.isPowerupActive()
                                       && player.getType()
                                                .equals(Player.Type.PACMAN)
                                       || !extrasService.isPowerupActive()
                                       && player.getType()
                                                .equals(Player.Type.GHOST)) {
                                       collisions.forEach(collision -> {
                                           Player collidedWith =
                                               playerRepository.update(UUID.fromString(
                                                   collision.getUuid()),
                                                   p -> p.toBuilder()
                                                         .setState(Player.State.DISCONNECTED)
                                                         .build());
                                           playersProcessor.onNext(collidedWith);
                                       });
                                       playerBuilder.setScore(player.getScore() + 100 * collisions.size());
                                   }
                               }
                               else if (player.getType() == Player.Type.PACMAN && extrasService.check(
                                   position.getX(),
                                   position.getY()) > 0) {
                                   playerBuilder.setScore(player.getScore() + 1);
                                   // scoreProcessor.onNext({player, score: player.getScore() + 1});
                               }

                               return playerBuilder.setTimestamp(Instant.now().toEpochMilli()).build();
                           });


                           if (updatedPlayer.getState() == Player.State.DISCONNECTED) {
                               playerRepository.delete(uuid);
                           }

                           playersProcessor.onNext(updatedPlayer);
                       })
                       .then()
                   );
    }

    @Override
    public Flux<Player> players() {
        return playersProcessor;
    }

    @Override
    public Mono<Player> createPlayer(String nickname) {
        return Mono
            .subscriberContext()
            .map(c -> c.<UUID>get("uuid"))
            .map((uuid) -> playerRepository.save(uuid, () -> {
                var score = 0;
                var playerType = generatePlayerType();
                var playerPosition = findBestStartingPosition(playerType);
                return Player.newBuilder()
                             .setLocation(Location.newBuilder()
                                                  .setDirection(Direction.RIGHT)
                                                  .setPosition(playerPosition.toBuilder()
                                                  .setX(playerPosition.getX() * 100)
                                                  .setY(playerPosition.getY() * 100)))
                             .setNickname(nickname)
                             .setState(Player.State.CONNECTED)
                             .setScore(score)
                             .setType(playerType)
                             .setUuid(uuid.toString())
                             .setTimestamp(Instant.now().toEpochMilli())
                             .build();
            }))
            .doAfterSuccessOrError((player, t) -> {
                if (player != null) {
                    playersSink.next(player);
                }
            });
    }

    @Override
    public Mono<Void> disconnectPlayer() {
        return Mono.subscriberContext()
                   .map(c -> c.<UUID>get("uuid"))
                   .doOnNext(uuid -> {
                       LOGGER.info("Disconnecting Player: {}", uuid);
                       Player player = playerRepository.delete(uuid);
                       if (player != null) {
                           playersSink.next(player.toBuilder()
                                                  .setState(Player.State.DISCONNECTED)
                                                  .build());
                       }
                   })
                   .then();
    }

    private Player.Type generatePlayerType() {
        var manCount = 0;
        var ghostCount = 0;
        var players = playerRepository.findAll();

        for (Player player : players) {

            if (player.getType() == Player.Type.PACMAN) {
                manCount++;
            }
            else if (player.getType() == Player.Type.GHOST) {
                ghostCount++;
            }
        }

        if (ghostCount < manCount) {
            return Player.Type.GHOST;
        }
        else {
            return Player.Type.PACMAN;
        }
    }

    private Point findBestStartingPosition(Player.Type playerType) {

        var players = playerRepository.findAll()
            .stream()
            .filter(p -> p.getType() != playerType)
            .collect(Collectors.toList());

        while(true) {
            var point = mapService.getRandomPoint();
            if (players.size() == 0) {
                return point;
            }
            for (Player player : players) {
                if (playerType != player.getType()) {
                    var dist = distance(player.getLocation()
                        .getPosition(), point);
                    if (dist > 5) {
                        return point;
                    }
                }
            }
        }
    }

    private float distance(Point p1, Point p2) {
        var d1 = p1.getX() - p2.getX();
        var d2 = p1.getY() - p2.getY();
        return d1 * d1 + d2 * d2;
    }
}
