package org.coinen.reactive.pacman.controller.rsocket.support;

import io.rsocket.Closeable;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

public class ConnectingRSocket implements RSocket {

    private final MonoProcessor<RSocket> rSocketMono;

    public ConnectingRSocket(Mono<RSocket> rSocketMono) {
        this.rSocketMono = rSocketMono.toProcessor();
    }


    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        if (rSocketMono.isSuccess()) {
            return rSocketMono.peek().fireAndForget(payload);
        }
        else {
            return rSocketMono.flatMap(rSocket -> rSocket.fireAndForget(payload));
        }
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        if (rSocketMono.isSuccess()) {
            return rSocketMono.peek().requestResponse(payload);
        }
        else {
            return rSocketMono.flatMap(rSocket -> rSocket.requestResponse(payload));
        }
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        if (rSocketMono.isSuccess()) {
            return rSocketMono.peek().requestStream(payload);
        }
        else {
            return rSocketMono.flatMapMany(rSocket -> rSocket.requestStream(payload));
        }
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        if (rSocketMono.isSuccess()) {
            return rSocketMono.peek().requestChannel(payloads);
        }
        else {
            return rSocketMono.flatMapMany(rSocket -> rSocket.requestChannel(payloads));
        }
    }

    @Override
    public Mono<Void> metadataPush(Payload payload) {
        if (rSocketMono.isSuccess()) {
            return rSocketMono.peek().metadataPush(payload);
        }
        else {
            return rSocketMono.flatMap(rSocket -> rSocket.metadataPush(payload));
        }
    }

    @Override
    public double availability() {
        return rSocketMono.isSuccess() ? rSocketMono.peek().availability() : 0d;
    }

    @Override
    public void dispose() {
        if (rSocketMono.isSuccess()) {
            rSocketMono.peek()
                       .dispose();
        }
        else {
            rSocketMono.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return rSocketMono.isSuccess() ? rSocketMono.peek().isDisposed() : rSocketMono.isDisposed();
    }

    @Override
    public Mono<Void> onClose() {
        if (rSocketMono.isSuccess()) {
            return rSocketMono.peek().onClose();
        }
        else {
            return rSocketMono.flatMap(Closeable::onClose);
        }
    }
}
