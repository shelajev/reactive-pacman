package org.coinen.reactive.pacman.repository.support;

import org.coinen.reactive.pacman.repository.ExtrasRepository;
import org.roaringbitmap.PeekableIntIterator;
import org.roaringbitmap.RoaringBitmap;

public class InMemoryExtrasRepository implements ExtrasRepository {

    final int tileSize = 100;
    final int mapWidth = 60;
    final int mapHeight = 60;
    final int offset = 11;

    final RoaringBitmap bitmap = new RoaringBitmap();

    @Override
    public int collideExtra(float x, float y) {
        var i = Math.round(x / tileSize);
        var j = Math.round(y / tileSize);

        var flattenPosition = i + j * mapWidth;

        if (bitmap.checkedRemove(flattenPosition)) {
            return flattenPosition;
        } else if(bitmap.checkedRemove(-flattenPosition)) {
            return -flattenPosition;
        }

        return 0;
    }

    @Override
    public int createExtra(int size) {
        var nextPosition = 0;
        do {
            nextPosition = randomPosition(mapWidth, mapHeight, offset);
        } while(bitmap.contains(nextPosition) || bitmap.contains(-nextPosition));

        var powerupCount = 0;

        PeekableIntIterator iterator = bitmap.getIntIterator();

        while (iterator.hasNext()) {
            if (iterator.next() < 0) {
                powerupCount++;
            }
        }

        var powerupAllowed = false;
        var totalSpace =
            (mapWidth - 2 * offset + 1) * (mapHeight - 2 * offset + 1);
        if (size <= 2) {
            powerupAllowed = powerupCount < Math.ceil(totalSpace / 360);
        }
        else if (size <= 4) {
            powerupAllowed = powerupCount < Math.ceil(totalSpace / 900);
        }
        else {
            powerupAllowed = powerupCount == 0 && Math.random() < 0.02;
        }

        if (powerupAllowed) {
            bitmap.add(-nextPosition);
            return -nextPosition;
        }
        else {
            bitmap.add(nextPosition);
            return nextPosition;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Integer> finaAll() {
        return bitmap;
    }

    @Override
    public void saveAll(int[] extras) {
        bitmap.add(extras);
    }


    static int randomPosition(int width, int height, int offset) {
        return (int) (Math.floor(Math.random() * (width - 2 * offset + 1) + offset) +  Math.floor(Math.random() * (height - 2 * offset + 1) + offset) * height);
    }
}
