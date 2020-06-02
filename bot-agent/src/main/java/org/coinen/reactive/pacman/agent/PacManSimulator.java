package org.coinen.reactive.pacman.agent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.coinen.pacman.ExtrasServiceClient;
import org.coinen.pacman.GameServiceClient;
import org.coinen.pacman.MapServiceServer;
import org.coinen.pacman.Nickname;
import org.coinen.pacman.Player;
import org.coinen.pacman.PlayerServiceClient;
import org.coinen.pacman.learning.KnowledgeServiceClient;
import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core.GameUtils;
import org.coinen.reactive.pacman.agent.core._G_;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import org.coinen.reactive.pacman.agent.repository.TemporaryHistoryRepository;
import org.coinen.reactive.pacman.agent.repository.impl.InMemoryTemporaryHistoryRepositoryImpl;
import org.coinen.reactive.pacman.agent.repository.impl.RemoteKnowledgeRepository;
import org.coinen.reactive.pacman.agent.service.DecisionService;
import org.coinen.reactive.pacman.agent.service.GameEngineService;
import org.coinen.reactive.pacman.agent.service.LearningService;
import org.coinen.reactive.pacman.agent.service.impl.DefaultGameEngineService;
import org.coinen.reactive.pacman.agent.service.impl.QLearningDecisionService;
import org.coinen.reactive.pacman.agent.service.impl.QLearningLearningService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One simulator can run one instance of PacMan-vs-Ghosts game.
 * <p>
 * Can be used for both head/less games.
 *
 * @author Jimmy
 */
public class PacManSimulator {

    static final ByteBuf PACMAN_PLAYER;
    static {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeCharSequence(Player.Type.PACMAN.name(), Charset.defaultCharset());
        PACMAN_PLAYER = buffer.retain();
    }


    public static void main(String[] args) throws InterruptedException {
        RSocket remoteServerKnowledgeBase = RSocketFactory.connect()
                .frameDecoder(PayloadDecoder.ZERO_COPY)
                .transport(TcpClientTransport.create("dinoman.rsocket.cloud", 9099))
                .start()
                .block();
        TemporaryHistoryRepository temporaryHistoryRepository = new InMemoryTemporaryHistoryRepositoryImpl();
        KnowledgeRepository knowledgeRepository = new RemoteKnowledgeRepository(new KnowledgeServiceClient(remoteServerKnowledgeBase));
//        KnowledgeRepository knowledgeRepository = new InMemoryKnowledgeRepository();
        RSocket rSocket = RSocketConnector.create()
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .keepAlive(Duration.ofSeconds(1), Duration.ofDays(1))
                .acceptor(new SocketAcceptor() {
                    @Override
                    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
                        return Mono.just(new MapServiceServer((map, metadata) -> {
                            GameUtils.loadDistances();

                            GameServiceClient gameServiceClient = new GameServiceClient(sendingSocket);
                            PlayerServiceClient locationServiceClient = new PlayerServiceClient(sendingSocket);
                            ExtrasServiceClient extrasService = new ExtrasServiceClient(sendingSocket);
                            LearningService learningService = new QLearningLearningService(knowledgeRepository, temporaryHistoryRepository);

                            var disposable = Mono.defer(() -> gameServiceClient
                                    .start(
                                        Nickname.newBuilder()
                                                .setValue("MsPacMan" + ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE))
                                                .build(),
                                        PACMAN_PLAYER.retainedSlice()
                                    ))
                                    .flatMapMany(config -> {
                                        var game = new _G_();
                                        game.newGame(map, config);

                                        DecisionService decisionService = new QLearningDecisionService(temporaryHistoryRepository, knowledgeRepository, game);
                                        GameEngineService gameEngineService = new DefaultGameEngineService(map, config, game, locationServiceClient, gameServiceClient, extrasService);

                                        // INIT RANDOMNESS
                                        G.rnd = new Random();

                                        return processingFlow(
                                            decisionService,
                                                gameEngineService,
                                                learningService,
                                                knowledgeRepository
                                        );

                                    })
                                    .doOnEach(System.out::println)
                                    .repeat()
                                    .subscribe(System.out::print, Throwable::printStackTrace, () -> System.out.println("done"));

                            sendingSocket.onClose()
                                    .doOnTerminate(disposable::dispose)
                                    .subscribe();

                            // INITIALIZE THE SIMULATION


                            return Mono.empty();
                        }, Optional.empty(), Optional.empty(), Optional.empty()));
                    }
                })
                .connect(WebsocketClientTransport.create("dinoman.rsocket.cloud", 3000))
                .block();

        rSocket.onClose().block();
    }

    private static Publisher<? extends Void> processingFlow(
            DecisionService decisionService,
            GameEngineService gameEngineService,
            LearningService learningService,
            KnowledgeRepository knowledgeRepository) {
        return decisionService
                .decide()
                .transform(gameEngineService::run)
                .transform(learningService::learn)
                .as(knowledgeRepository::educate);
    }
}
