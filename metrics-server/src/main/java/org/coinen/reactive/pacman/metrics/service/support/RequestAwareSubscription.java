package org.coinen.reactive.pacman.metrics.service.support;

import org.reactivestreams.Subscription;
import reactor.core.publisher.Operators;

final class RequestAwareSubscription implements Subscription {
    final Subscription delegate;
    long requested = 0;
    long collected = 0;

    RequestAwareSubscription(Subscription delegate) {
        this.delegate = delegate;
    }

    @Override
    public void request(long n) {
        collected = Operators.addCap(collected, n);
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    public void tryFlush(long threshold, long received) {
        long pending = requested - received;

        if (pending > threshold) {
            return;
        }

        long n = Math.min(collected, threshold);

        if (n == 0) {
            return;
        }

        collected = 0;
        requested = Operators.addCap(requested, n);
        delegate.request(n);
    }
}
