package org.coinen.reactive.pacman.metrics.grpc;

import java.nio.ByteBuffer;
import java.time.Instant;

import io.grpc.Metadata;

public abstract class Constants {

    private static class InstantBinaryMarshaller
        implements Metadata.BinaryMarshaller<Instant> {

        private static ThreadLocal<ByteBuffer> THREAD_LOCAL_BUFFER = ThreadLocal.withInitial(() -> ByteBuffer.allocate(Long.BYTES + Integer.BYTES));

        @Override
        public byte[] toBytes(Instant value) {
            long second = value.getEpochSecond();
            int nano = value.getNano();

            return THREAD_LOCAL_BUFFER
                .get()
                .putLong(0, second)
                .putInt(Long.BYTES, nano)
                .array();
        }

        @Override
        public Instant parseBytes(byte[] serialized) {
            ByteBuffer byteBuffer = THREAD_LOCAL_BUFFER
                .get()
                .put(serialized, 0, Long.BYTES + Integer.BYTES)
                .flip();

            long seconds = byteBuffer.getLong(0);
            int nanos = byteBuffer.getInt(Long.BYTES);

            return Instant.ofEpochSecond(seconds, nanos);
        }
    }

    static final Metadata.Key<Instant> INSTANT_KEY = Metadata.Key.of("time-bin", new InstantBinaryMarshaller());
}
