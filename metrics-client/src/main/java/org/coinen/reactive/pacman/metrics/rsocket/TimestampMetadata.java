package org.coinen.reactive.pacman.metrics.rsocket;

import java.time.Instant;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

abstract class TimestampMetadata {

    private static final int METADATA_OFFSET = Long.BYTES + Integer.BYTES;

    private TimestampMetadata() {}


    static ByteBuf encode(ByteBufAllocator allocator, Instant time, ByteBuf metadata) {
        ByteBuf byteBuf = allocator.buffer();

        byteBuf.writeBytes(metadata, 0, metadata.readableBytes());

        byteBuf.writeLong(time.getEpochSecond());
        byteBuf.writeInt(time.getNano());

        return byteBuf;
    }

    static Instant time(ByteBuf metadata) {
        int metadataLength = metadata.readableBytes() - METADATA_OFFSET;
        return metadataLength >= 0
            ? Instant.ofEpochSecond(metadata.getLong(metadataLength), metadata.getInt(metadataLength + Long.BYTES))
            : null;
    }

    static ByteBuf metadata(ByteBuf byteBuf) {
        int metadataLength = byteBuf.readableBytes() - METADATA_OFFSET;

        return metadataLength > 0 ? byteBuf.slice(METADATA_OFFSET, metadataLength) : Unpooled.EMPTY_BUFFER;
    }
}
