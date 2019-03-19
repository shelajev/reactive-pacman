package org.coinen.reactive.pacman.controller.grpc;

import java.util.UUID;

public class UUIDHolder {
    static final ThreadLocal<UUID> UUID_THREAD_LOCAL = new ThreadLocal<>();

    public static UUID get() {
        return UUID_THREAD_LOCAL.get();
    }

    public static void set(UUID uuid) {
        UUID_THREAD_LOCAL.set(uuid);
    }
}
