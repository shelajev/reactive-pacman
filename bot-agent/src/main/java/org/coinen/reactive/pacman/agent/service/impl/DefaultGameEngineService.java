package org.coinen.reactive.pacman.agent.service.impl;

import com.google.protobuf.Empty;
import io.netty.buffer.Unpooled;
import org.coinen.pacman.*;
import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core.Game;
import org.coinen.reactive.pacman.agent.core._G_;
import org.coinen.reactive.pacman.agent.model.Decision;
import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.model.Outcome;
import org.coinen.reactive.pacman.agent.service.GameEngineService;
import org.coinen.reactive.pacman.agent.service.utils.GameUtils;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class DefaultGameEngineService implements GameEngineService {

    final _G_ game;
    final PlayerService playerService;
    final GameService gameService;
    final ExtrasService extrasService;
    private final Map map;

    Config gameConfig;
    DirectProcessor<Location> outgoingChannel;

    public DefaultGameEngineService(
        Map map,
        Config gameConfig,
        _G_ game,
        PlayerService playerService,
        GameService gameService,
        ExtrasService extrasService
    ) {
        this.map = map;
        this.gameConfig = gameConfig;
        this.game = game;
        this.playerService = playerService;
        this.gameService = gameService;
        this.extrasService = extrasService;
    }

    @Override
    public Flux<Outcome> run(Flux<Decision> decisionFlux) {
        DirectProcessor<Location> directProcessor = DirectProcessor.create();
        Mono<Empty> locate = playerService.locate(directProcessor.onBackpressureBuffer(), Unpooled.EMPTY_BUFFER);
        Flux<Player> players = playerService.players(Empty.getDefaultInstance(), Unpooled.EMPTY_BUFFER);
        Flux<Extra> extras = extrasService.extras(Empty.getDefaultInstance(), Unpooled.EMPTY_BUFFER);

        var action = game.getPossiblePacManDirs()[0];
        var initState = new GameState(GameUtils.getNextPill(game, Game.UP), GameUtils.getNextPill(game, Game.RIGHT), GameUtils.getNextPill(game, Game.DOWN), GameUtils.getNextPill(game, Game.LEFT),
                GameUtils.getNextGhost(game, Game.UP), GameUtils.getNextGhost(game, Game.RIGHT), GameUtils.getNextGhost(game, Game.DOWN), GameUtils.getNextGhost(game, Game.LEFT),
                GameUtils.getNextPowerPill(game, Game.UP), GameUtils.getNextPowerPill(game, Game.RIGHT), GameUtils.getNextPowerPill(game, Game.DOWN), GameUtils.getNextPowerPill(game, Game.LEFT),
                GameUtils.getNextEdibleGhost(game, Game.UP), GameUtils.getNextEdibleGhost(game, Game.RIGHT), GameUtils.getNextEdibleGhost(game, Game.DOWN), GameUtils.getNextEdibleGhost(game, Game.LEFT),
                GameUtils.getNextIntersection(game, Game.UP), GameUtils.getNextIntersection(game, Game.RIGHT), GameUtils.getNextIntersection(game, Game.DOWN), GameUtils.getNextIntersection(game, Game.LEFT));

        var firstOutcome = new Outcome(initState, 0, new Decision(org.coinen.reactive.pacman.agent.controllers.Direction.forIndex(action)), Outcome.Type.NONE);

        this.outgoingChannel = directProcessor;
        return decisionFlux
                .transform(this::doStartActing)
                .subscribeOn(Schedulers.newSingle("Game Thread"))
                .startWith(firstOutcome)
                .takeUntilOther(Flux.merge(
                    extras.doOnNext(this::processExtrasChanges).thenMany(Flux.empty()),
                    locate.thenMany(Flux.empty()),
                    players.doOnNext(this::processExternalPlayers).thenMany(Flux.empty())
                ));
    }

    Flux<Outcome> doStartActing(Flux<Decision> decisionFlux) {
        return
decisionFlux.handle((decision, sink) -> {
    G.Node lastNode = G.maze.graph[game.getCurPacManLoc()];

    int gain = game.advanceGame(decision);

    G.Node node = G.maze.graph[game.getCurPacManLoc()];
    org.coinen.reactive.pacman.agent.controllers.Direction direction = org.coinen.reactive.pacman.agent.controllers.Direction.forIndex(game.getCurPacManDir());

    // VISUALIZE GAME
    movePacMan(direction, lastNode, node);

    if (gain == Integer.MIN_VALUE) {
        sink.next(new Outcome(GameUtils.captureState(game), Integer.MIN_VALUE, decision, Outcome.Type.KILLED));
        sink.complete();
    } else {
        sink.next(new Outcome(GameUtils.captureState(game), gain, decision, Outcome.Type.PILL_EATEN));
    }
});
    }

    void movePacMan(org.coinen.reactive.pacman.agent.controllers.Direction direction, G.Node from, G.Node to) {
        final DirectProcessor<Location> outgoingChannel = this.outgoingChannel;
        int fromY = from.y * 100;
        int toY = to.y * 100;
        int fromX = from.x * 100;
        int toX = to.x * 100;
        switch (direction) {
            case UP: {
                for (int i = fromY; i >= toY; i--) {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outgoingChannel.onNext(
                            Location
                                    .newBuilder()
                                    .setPosition(Point.newBuilder().setX(toX).setY(i))
                                    .setDirection(org.coinen.pacman.Direction.valueOf(direction.name()))
                                    .build()
                    );
                }
                break;
            }
            case DOWN: {
                for (int i = fromY; i <= toY; i++) {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outgoingChannel.onNext(
                            Location
                                    .newBuilder()
                                    .setPosition(Point.newBuilder().setX(toX).setY(i))
                                    .setDirection(org.coinen.pacman.Direction.valueOf(direction.name()))
                                    .build()
                    );
                }
                break;
            }
            case LEFT: {
                for (int i = fromX; i >= toX; i--) {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outgoingChannel.onNext(
                            Location
                                    .newBuilder()
                                    .setPosition(Point.newBuilder().setX(i).setY(toY))
                                    .setDirection(org.coinen.pacman.Direction.valueOf(direction.name()))
                                    .build()
                    );
                }
                break;
            }
            case RIGHT: {
                for (int i = fromX; i <= toX; i++) {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outgoingChannel.onNext(
                            Location
                                    .newBuilder()
                                    .setPosition(Point.newBuilder().setX(i).setY(fromY))
                                    .setDirection(org.coinen.pacman.Direction.valueOf(direction.name()))
                                    .build()
                    );
                }
                break;
            }
        }
    }

    void processExtrasChanges(Extra e) {
        int last = e.getLast();
        int lastIndex = Math.abs(last);
        G.Node lastNode = G.maze.graph[lastIndex];
        if (Math.signum(last) == -1) {
            int powerPillIndex = lastNode.powerPillIndex;
            synchronized (game.powerPills) {
                game.powerPills.clear(powerPillIndex);
            }
            lastNode.powerPillIndex = -1;
        } else {
            int pillIndex = lastNode.pillIndex;
            synchronized (game.pills) {
                game.pills.clear(pillIndex);
            }
            lastNode.pillIndex = -1;
        }

        int current = e.getCurrent();
        int currentIndex = Math.abs(current);
        G.Node currentNode = G.maze.graph[currentIndex];
        if (Math.signum(current) == -1) {
            int nextPowerPillIndex = game.powerPills.nextClearBit(0);
            synchronized (game.powerPills) {
                game.powerPills.set(nextPowerPillIndex, true);
            }
            currentNode.powerPillIndex = nextPowerPillIndex;
        } else {
            int nextPillIndex = game.pills.nextClearBit(0);
            synchronized (game.pills) {
                game.pills.set(nextPillIndex, true);
            }
            currentNode.pillIndex = nextPillIndex;
        }
    }

    void processExternalPlayers(Player p) {
        switch (p.getType()) {
            case GHOST: {
                String uuid = p.getUuid();
                Location location = p.getLocation();
                Point position = location.getPosition();
                org.coinen.reactive.pacman.agent.controllers.Direction direction = org.coinen.reactive.pacman.agent.controllers.Direction.valueOf(location.getDirection().name());
                int locationIndex = (int) (position.getX() / 100) + (int) (position.getY() * game.getWidth() / 100);
                switch (p.getState()) {
                    case CONNECTED: {
                        game.curGhosts.put(uuid, new G.Ghost(uuid, locationIndex, direction.index));
                        break;
                    }
                    case ACTIVE: {
                        G.Ghost ghost = game.curGhosts.get(uuid);
                        if (ghost == null) {
                            break;
                        }
                        ghost.location = locationIndex;
                        ghost.direction = direction.index;
                        break;
                    }
                    case DISCONNECTED: {
                        game.curGhosts.remove(uuid);
                        break;
                    }
                }
                break;
            }
            case PACMAN: {
                if (gameConfig.getPlayer().getUuid().equals(p.getUuid()) && p.getState() == Player.State.DISCONNECTED) {
                    System.out.println("Killed + " + p);
                    game.kill();
                }
                break;
            }
        }
    }
}
