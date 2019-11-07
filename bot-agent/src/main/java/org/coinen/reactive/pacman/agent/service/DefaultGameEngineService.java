package org.coinen.reactive.pacman.agent.service;

import com.google.protobuf.Empty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.coinen.pacman.*;
import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core.Game;
import org.coinen.reactive.pacman.agent.core._G_;
import org.coinen.reactive.pacman.agent.model.Decision;
import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.model.Outcome;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import org.coinen.reactive.pacman.agent.service.utils.GameUtils;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.concurrent.ThreadLocalRandom;

public class DefaultGameEngineService implements GameEngineService {

    final _G_ game;
    final DecisionService decisionService;
    final PlayerService playerService;
    final GameService gameService;
    final ExtrasService extrasService;
    private final Map map;

    Config gameConfig;
    DirectProcessor<Location> outgoingChannel;

    public DefaultGameEngineService(
        Map map,
        KnowledgeRepository knowledgeRepository,
        PlayerService playerService,
        GameService gameService,
        ExtrasService extrasService
    ) {
        this.map = map;
        this.game = new _G_();
        this.decisionService = new QLearningDecisionService(knowledgeRepository, game);
        this.playerService = playerService;
        this.gameService = gameService;
        this.extrasService = extrasService;
    }

    @Override
    public Flux<Outcome> start() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeCharSequence(Player.Type.PACMAN.toString(), Charset.defaultCharset());
        return gameService
                .start(
                        Nickname.newBuilder()
                                .setValue("MsPacMan" + ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE))
                                .build(),
                        buffer
                )
                .flatMapMany(config -> {
                    game.newGame(map, config);
                    DirectProcessor<Location> directProcessor = DirectProcessor.create();
                    Mono<Empty> locate = playerService.locate(directProcessor.onBackpressureBuffer(), Unpooled.EMPTY_BUFFER);
                    Flux<Player> players = playerService.players(Empty.getDefaultInstance(), Unpooled.EMPTY_BUFFER);
                    Flux<Extra> extras = extrasService.extras(Empty.getDefaultInstance(), Unpooled.EMPTY_BUFFER);

                    var action = game.getPossiblePacManDirs(true)[0];
                    var initState = new GameState(0, GameUtils.getNextPill(game, Game.UP), GameUtils.getNextPill(game, Game.RIGHT), GameUtils.getNextPill(game, Game.DOWN), GameUtils.getNextPill(game, Game.LEFT),
                            GameUtils.getNextGhost(game, Game.UP), GameUtils.getNextGhost(game, Game.RIGHT), GameUtils.getNextGhost(game, Game.DOWN), GameUtils.getNextGhost(game, Game.LEFT),
                            GameUtils.getNextPowerPill(game, Game.UP), GameUtils.getNextPowerPill(game, Game.RIGHT), GameUtils.getNextPowerPill(game, Game.DOWN), GameUtils.getNextPowerPill(game, Game.LEFT),
                            GameUtils.getNextEdibleGhost(game, Game.UP), GameUtils.getNextEdibleGhost(game, Game.RIGHT), GameUtils.getNextEdibleGhost(game, Game.DOWN), GameUtils.getNextEdibleGhost(game, Game.LEFT),
                            GameUtils.getNextIntersection(game, Game.UP), GameUtils.getNextIntersection(game, Game.RIGHT), GameUtils.getNextIntersection(game, Game.DOWN), GameUtils.getNextIntersection(game, Game.LEFT));

                    var firstOutcome = new Outcome(initState, 0, new Decision(org.coinen.reactive.pacman.agent.controllers.Direction.forIndex(action)), Outcome.Type.NONE);

                    this.gameConfig = config;
                    this.outgoingChannel = directProcessor;
                    return Flux.merge(
                            extras.doOnNext(this::processExtrasChanges).thenMany(Flux.empty()),
                            locate.thenMany(Flux.empty()),
                            players.doOnNext(this::processExternalPlayers).thenMany(Flux.empty()),
                            doStartActing()
                    ).startWith(firstOutcome, firstOutcome);
                });
    }

    Flux<Outcome> doStartActing() {
        return Flux.generate(sink -> {
            G.Node lastNode = G.maze.graph[game.getCurPacManLoc()];

            Decision decision = decisionService.decide();
            // GESTION DE HILOS
            // WAKE UP THINKING THREADS

            // GIVE THINKING TIME


            int gain = game.advanceGame(decision);

            if (gain == Integer.MIN_VALUE) {
                sink.next(new Outcome(GameUtils.captureState(game), Integer.MIN_VALUE, decision, Outcome.Type.KILLED));
                sink.complete();
                return;
            }


            // VISUALIZE GAME
            G.Node node = G.maze.graph[game.getCurPacManLoc()];
            org.coinen.reactive.pacman.agent.controllers.Direction direction = org.coinen.reactive.pacman.agent.controllers.Direction.forIndex(game.getCurPacManDir());

            movePacMan(direction, lastNode, node);


            sink.next(new Outcome(GameUtils.captureState(game), gain, decision, Outcome.Type.NONE));
        });
    }

    void movePacMan(org.coinen.reactive.pacman.agent.controllers.Direction direction, G.Node from, G.Node to) {
        final DirectProcessor<Location> outgoingChannel = this.outgoingChannel;
        switch (direction) {
            case UP: {
                for (int i = (from.y * 100); i >= (to.y * 100); i--) {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outgoingChannel.onNext(
                            Location
                                    .newBuilder()
                                    .setPosition(Point.newBuilder().setX(to.x * 100).setY(i))
                                    .setDirection(org.coinen.pacman.Direction.valueOf(direction.name()))
                                    .build()
                    );
                }
            }
            case DOWN: {
                for (int i = from.y * 100; i <= to.y * 100; i++) {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outgoingChannel.onNext(
                            Location
                                    .newBuilder()
                                    .setPosition(Point.newBuilder().setX((to.x * 100)).setY(i))
                                    .setDirection(org.coinen.pacman.Direction.valueOf(direction.name()))
                                    .build()
                    );
                }
            }
            case LEFT: {
                for (int i = from.x * 100; i >= to.x * 100; i--) {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outgoingChannel.onNext(
                            Location
                                    .newBuilder()
                                    .setPosition(Point.newBuilder().setX(i).setY(to.y * 100))
                                    .setDirection(org.coinen.pacman.Direction.valueOf(direction.name()))
                                    .build()
                    );
                }
            }
            case RIGHT: {
                for (int i = from.x * 100; i <= to.x * 100; i++) {
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outgoingChannel.onNext(
                            Location
                                    .newBuilder()
                                    .setPosition(Point.newBuilder().setX(i).setY(from.y * 100))
                                    .setDirection(org.coinen.pacman.Direction.valueOf(direction.name()))
                                    .build()
                    );
                }
            }
        }
    }

    void processExtrasChanges(Extra e) {
        int last = e.getLast();
        int lastIndex = Math.abs(last);
        G.Node lastNode = G.maze.graph[lastIndex];
        if (Math.signum(last) == -1) {
            int powerPillIndex = lastNode.powerPillIndex;
            game.powerPills.clear(powerPillIndex);
            lastNode.powerPillIndex = -1;
        } else {
            int pillIndex = lastNode.pillIndex;
            game.pills.clear(pillIndex);
            lastNode.pillIndex = -1;
        }

        int current = e.getCurrent();
        int currentIndex = Math.abs(current);
        G.Node currentNode = G.maze.graph[currentIndex];
        if (Math.signum(current) == -1) {
            int nextPowerPillIndex = game.powerPills.nextClearBit(0);
            game.powerPills.set(nextPowerPillIndex, true);
            currentNode.powerPillIndex = nextPowerPillIndex;
        } else {
            int nextPillIndex = game.pills.nextClearBit(0);
            game.pills.set(nextPillIndex, true);
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
                        ghost.location = locationIndex;
                        ghost.direction = direction.index;
                        break;
                    }
                    case DISCONNECTED: {
                        game.curGhosts.remove(uuid);
                        break;
                    }
                }
                System.out.println("Update + " + p);
                game.feast();
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
