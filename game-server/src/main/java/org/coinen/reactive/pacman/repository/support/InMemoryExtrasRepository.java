package org.coinen.reactive.pacman.repository.support;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.coinen.reactive.pacman.repository.ExtrasRepository;
import org.roaringbitmap.RoaringBitmap;
import static org.coinen.reactive.pacman.repository.ExtrasRepository.randomPosition;

public class InMemoryExtrasRepository implements ExtrasRepository {

    final int tileSize = 100;
    final int mapWidth = 60;
    final int mapHeight = 60;
    final int offset = 11;
    final int totalSpace = (mapWidth - 2 * offset + 1) * (mapHeight - 2 * offset + 1);

    final RoaringBitmap bitmap = new RoaringBitmap();
    volatile int powerUpExtrasCount = 0;
    static final AtomicIntegerFieldUpdater<InMemoryExtrasRepository> POWER_UP_EXTRAS_COUNT =
        AtomicIntegerFieldUpdater.newUpdater(InMemoryExtrasRepository.class, "powerUpExtrasCount");

    public InMemoryExtrasRepository() {
        bitmap.add(generate(mapWidth, mapHeight, offset));
    }

    @Override
    public int collideExtra(float x, float y) {
        var i = Math.round(x / tileSize);
        var j = Math.round(y / tileSize);

        var flattenPosition = i + j * mapWidth;

        if (bitmap.checkedRemove(flattenPosition)) {
            return flattenPosition;
        } else if(bitmap.checkedRemove(-flattenPosition)) {
            POWER_UP_EXTRAS_COUNT.decrementAndGet(this);
            return -flattenPosition;
        }

        return 0;
    }

    @Override
    public int createExtra(int size) {
        var powerUpAllowed = false;

        while (true) {
            var initialCnt = powerUpExtrasCount;
            int nextCnt = initialCnt;

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

            if (POWER_UP_EXTRAS_COUNT.compareAndSet(this, initialCnt, nextCnt)) {
                break;
            }
        }

        if (powerUpAllowed) {
            var nextPosition = 0;
            do {
                nextPosition = randomPosition(mapWidth, mapHeight, offset);

            } while (!bitmap.checkedAdd(-nextPosition));
            return -nextPosition;
        }
        else {
            var nextPosition = 0;
            do {
                nextPosition = randomPosition(mapWidth, mapHeight, offset);

            } while (!bitmap.checkedAdd(nextPosition));
            return nextPosition;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Integer> finaAll() {
        return bitmap;
    }
}
