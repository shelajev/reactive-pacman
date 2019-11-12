package org.coinen.reactive.pacman.learning;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.protobuf.Empty;
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

public class Server {

    static final ConcurrentHashMap<String, Knowledge> knowledgeBase = new ConcurrentHashMap<>();
    static final Sink<ConcurrentMap<String, Knowledge>, Publisher<ConcurrentMap<String, Knowledge>>> sink = Sink.asPublisher(AsPublisher.WITH_FANOUT);

    public static Flow<Knowledge, ConcurrentMap<String, Knowledge>, NotUsed> handleRequest() {
        return Flow.<Knowledge>create()
            .scan(knowledgeBase, (a, knowledge) -> {
                knowledgeBase.put(knowledge.getUuid(), knowledge);

                return knowledgeBase;
            });
    }

    public static void main(String[] args) throws Exception {
        CloseableChannel closeableChannel = null;

        try {
            closeableChannel = RSocketFactory.receive()
                    .acceptor((setup, sendingSocket) -> Mono.just(new KnowledgeServiceServer(new KnowledgeService() {
                        @Override
                        public Flux<KnowledgeBaseSnapshot> enrich(Publisher<Knowledge> messages, ByteBuf metadata) {
                            return Flux
                                    .from(
                                            Source.fromPublisher(messages)
                                                    .via(handleRequest())
                                                    .runWith(sink, Materializer.createMaterializer(ActorSystem.create()))
                                    )
                                    .onBackpressureDrop()
                                    .map(cm -> KnowledgeBaseSnapshot.newBuilder().addAllKnowledgeBase(cm.values()).build());
                        }

                        @Override
                        public Mono<KnowledgeBaseSnapshot> retrieveLatest(Empty message, ByteBuf metadata) {
                            return Mono.just(KnowledgeBaseSnapshot.newBuilder().addAllKnowledgeBase(knowledgeBase.values()).build());
                        }
                    }, Optional.empty(), Optional.empty(), Optional.empty())))
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
