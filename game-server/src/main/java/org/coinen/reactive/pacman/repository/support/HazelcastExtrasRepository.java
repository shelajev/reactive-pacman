package org.coinen.reactive.pacman.repository.support;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ILock;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import com.hazelcast.cp.lock.FencedLock;
import org.coinen.reactive.pacman.repository.ExtrasRepository;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import static org.coinen.reactive.pacman.repository.ExtrasRepository.randomPosition;

public class HazelcastExtrasRepository implements ExtrasRepository {

    final int tileSize = 100;
    final int mapWidth = 60;
    final int mapHeight = 60;
    final int offset = 11;
    final int totalSpace = (mapWidth - 2 * offset + 1) * (mapHeight - 2 * offset + 1);

    final ISet<Integer>            store;
    final IAtomicLong              powerUpExtrasCount;
    final DirectProcessor<Integer> remoteCollisionsFlux;
    final DirectProcessor<Integer> remoteProvisionsFlux;

    public HazelcastExtrasRepository(HazelcastInstance hazelcastInstance) {
        this.store = hazelcastInstance.getSet("extras");
        final ILock extras = hazelcastInstance.getLock("extras");
        if (extras.tryLock()) {
            try {
                if (this.store.isEmpty()) {
                    store.addAll(IntStream.of(generate(mapWidth, mapHeight, offset))
                                          .boxed()
                                          .collect(Collectors.toList()));
                }
            }
            finally {
                extras.unlock();
            }
        }
        this.powerUpExtrasCount = hazelcastInstance.getAtomicLong("powerUpExtrasCount");
        this.remoteCollisionsFlux = DirectProcessor.create();
        this.remoteProvisionsFlux = DirectProcessor.create();


        store.addItemListener(new ItemListener<>() {
            final FluxSink<Integer> remoteCollisionsSink = remoteCollisionsFlux.sink();
            final FluxSink<Integer> remoteProvisionsSink = remoteProvisionsFlux.sink();

            @Override
            public void itemAdded(ItemEvent<Integer> item) {
                if (!item.getMember().localMember()) {
                    remoteProvisionsSink.next(item.getItem());
                }
            }

            @Override
            public void itemRemoved(ItemEvent<Integer> item) {
                if (!item.getMember().localMember()) {
                    remoteCollisionsSink.next(item.getItem());
                }
            }
        }, true);
    }

    @Override
    public int collideExtra(float x, float y) {
        var i = Math.round(x / tileSize);
        var j = Math.round(y / tileSize);

        var flattenPosition = i + j * mapWidth;

        if (store.remove(flattenPosition)) {
            return flattenPosition;
        } else if(store.remove(-flattenPosition)) {
            powerUpExtrasCount.decrementAndGetAsync();
            return -flattenPosition;
        }

        return 0;
    }

    @Override
    public int createExtra(int size) {
        var powerUpAllowed = false;

        while (true) {
            var initialCnt = powerUpExtrasCount.get();
            long nextCnt = initialCnt;

            if (size <= 2) {
                powerUpAllowed = initialCnt < Math.ceil(totalSpace / 360);
            }
            else if (size <= 4) {
                powerUpAllowed = initialCnt < Math.ceil(totalSpace / 900);
            }
            else {
                powerUpAllowed = initialCnt == 0 && Math.random() < 0.02;
            }

            if (powerUpAllowed) {
                nextCnt++;
            }

            if (powerUpExtrasCount.compareAndSet(initialCnt, nextCnt)) {
                break;
            }
        }

        if (powerUpAllowed) {
            var nextPosition = 0;
            do {
                nextPosition = randomPosition(mapWidth, mapHeight, offset);

            } while (!store.add(-nextPosition));
            return -nextPosition;
        }
        else {
            var nextPosition = 0;
            do {
                nextPosition = randomPosition(mapWidth, mapHeight, offset);

            } while (!store.add(nextPosition));
            return nextPosition;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Integer> finaAll() {
        return store;
    }

    @Override
    public Flux<Integer> remoteCollisions() {
        return remoteCollisionsFlux;
    }

    @Override
    public Flux<Integer> remoteProvisions() {
        return remoteProvisionsFlux;
    }
}
