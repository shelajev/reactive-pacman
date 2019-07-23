package org.coinen.reactive.pacman.repository.support;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.MapListener;
import org.coinen.pacman.Player;
import org.coinen.reactive.pacman.repository.PlayerRepository;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;

public class HazelcastPlayerRepository implements PlayerRepository {

    final IMap<UUID, Player> store;
    final DirectProcessor<Player> remotePlayersUpdatesFlux;

    public HazelcastPlayerRepository(HazelcastInstance hazelcastInstance) {
        this.store = hazelcastInstance.getMap("players");
        this.remotePlayersUpdatesFlux = DirectProcessor.create();

        final var remotePlayersUpdatesSink = remotePlayersUpdatesFlux.sink();
        store.addEntryListener((EntryAddedListener<UUID, Player>) event -> {
            remotePlayersUpdatesSink.next(event.getValue());
        }, true);
        store.addEntryListener((EntryRemovedListener<UUID, Player>) event -> {
            remotePlayersUpdatesSink.next(event.getValue()
                                               .toBuilder()
                                               .setState(Player.State.DISCONNECTED)
                                               .build()
            );
        }, true);
    }

    @Override
    public Collection<Player> findAll() {
        return store.values();
    }

    @Override
    public Player findOne(UUID uuid) {
        return store.get(uuid);
    }

    @Override
    public Player update(UUID uuid, Function<Player, Player> playerUpdater) {
        return store.compute(uuid, (__, player) -> playerUpdater.apply(player));
    }

    @Override
    public Player save(UUID uuid, Supplier<? extends Player> playerSupplier) {
        return store.computeIfAbsent(uuid, __ -> playerSupplier.get());
    }

    @Override
    public Player delete(UUID uuid) {
        return store.remove(uuid);
    }

    @Override
    public int count() {
        return store.size();
    }

    @Override
    public Flux<Player> remotePlayers() {
        return remotePlayersUpdatesFlux;
    }
}
