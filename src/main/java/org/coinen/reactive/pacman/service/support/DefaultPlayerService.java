package org.coinen.reactive.pacman.service.support;

import java.time.Instant;
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
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import static org.coinen.reactive.pacman.service.support.DefaultMapService.getRandomIntInclusive;

public class DefaultPlayerService implements PlayerService {

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
    }

    @Override
    public Mono<Void> locate(Flux<Location> locationStream) {
        return Mono.subscriberContext()
                   .map(c -> c.<UUID>get("uuid"))
                   .flatMap(uuid -> locationStream
                       .doOnNext(location -> {
                           var time = Instant.now();
                           playersProcessor.onNext(playerRepository.update(uuid, player -> {
                               var builder = playerRepository.findOne(uuid)
                                                             .toBuilder()
                                                             .setTimestamp(time.toEpochMilli())
                                                             .setState(Player.State.ACTIVE)
                                                             .setLocation(location);

                               Point position = location.getPosition();

                               if (Math.signum(extrasService.check(position.getX(),
                                   position.getY())) == -1.0f) {
                                   builder.setScore(player.getScore() + 1);
                                   // scoreProcessor.onNext({player, score: player.getScore() + 1});
                               }

                               return builder.build();
                           }));
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
                             .build();
            }))
            .doAfterSuccessOrError((player, t) -> {
                if (player != null) {
                    playersSink.next(player);
                }
            });
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
        Point bestStart = null;
        var furthestDist = -1f;
        var starts = mapService.getTilesPositions();
        var players = playerRepository.findAll()
                                      .stream()
                                      .filter(p -> p.getType() != playerType)
                                      .collect(Collectors.toList());
        for (var i = 0; i < starts.size(); i++) {
            var start = starts.get(i);
            var closestPlayerDist = -1f;
            for (Player player : players) {
                if (playerType != player.getType()) {
                    var dist = distance(player.getLocation()
                                              .getPosition(), start);
                    if (closestPlayerDist == -1 || dist < closestPlayerDist) {
                        closestPlayerDist = dist;
                    }
                }
            }

            if (closestPlayerDist > furthestDist) {
                furthestDist = closestPlayerDist;
                bestStart = start;
            }
        }

        if (bestStart == null) {
            bestStart = starts.get(getRandomIntInclusive(0, starts.size() - 1));
        }

        return bestStart;
    }

    float distance(Point p1, Point p2) {
        var d1 = p1.getX() - p2.getX();
        var d2 = p1.getY() - p2.getY();
        return d1 * d1 + d2 * d2;
    }
}
