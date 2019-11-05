package org.coinen.reactive.pacman.learning;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.netty.buffer.ByteBuf;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import org.coinen.pacman.learning.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("Convert2MethodRef")
public class Server {

    static final ConcurrentHashMap<GameState, CaseStudy> caseStudies = new ConcurrentHashMap<>();
    static final Sink<ConcurrentMap<GameState, CaseStudy>, Publisher<ConcurrentMap<GameState, CaseStudy>>> sink = Sink.asPublisher(AsPublisher.WITH_FANOUT);

    public static Flow<CaseStudy, ConcurrentMap<GameState, CaseStudy>, NotUsed> handleRequest() {
        return Flow.<CaseStudy>create()
            .scan(caseStudies, (a, cs) -> {
                caseStudies.put(cs.getGameState(), cs);

                return caseStudies;
            });
    }

    public static void main(String[] args) throws Exception {
        CloseableChannel closeableChannel = null;

        try {
            closeableChannel = RSocketFactory.receive()
                    .acceptor((setup, sendingSocket) -> Mono.just(new LearningServiceServer((messages, metadata) -> Flux
                        .from(
                            Source.fromPublisher(messages)
                                .via(handleRequest())
                                .runWith(sink, Materializer.createMaterializer(ActorSystem.create()))
                        )
                        .onBackpressureDrop()
                        .map(cm -> StudyAggregate.newBuilder().addAllStudies(cm.values()).build()), Optional.empty(), Optional.empty(), Optional.empty())))
                    .transport(TcpServerTransport.create(9099))
                    .start()
                    .block();

            System.out.println("Press ENTER to stop.");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } finally {
            if (closeableChannel != null) {
                closeableChannel.dispose();
            }
        }
    }
}
