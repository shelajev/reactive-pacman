package org.coinen.reactive.pacman.controller.rsocket;

import com.google.protobuf.Empty;
import io.netty.buffer.ByteBuf;
import org.coinen.pacman.metrics.MetricsSnapshot;
import org.coinen.pacman.metrics.MetricsSnapshotHandler;
import org.coinen.pacman.metrics.MetricsSnapshotHandlerClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class MetricsSnapshotHandlerProxyController implements MetricsSnapshotHandler {
    static final Logger LOGGER = LoggerFactory.getLogger(MetricsSnapshotHandlerProxyController.class);

    final MetricsSnapshotHandlerClient delegate;

    public MetricsSnapshotHandlerProxyController(MetricsSnapshotHandlerClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<Empty> streamMetricsSnapshots(Publisher<MetricsSnapshot> messages, ByteBuf metadata) {
        return delegate.streamMetricsSnapshots(messages, metadata.retain());
    }

    @Override
    public Mono<Empty> sendMetricsSnapshot(MetricsSnapshot message, ByteBuf metadata) {
        return delegate.sendMetricsSnapshot(message, metadata.retain());
    }
}
