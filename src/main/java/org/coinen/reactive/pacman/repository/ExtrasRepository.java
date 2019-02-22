package org.coinen.reactive.pacman.repository;

public interface ExtrasRepository {

    int collideExtra(float x, float y);

    int createExtra(int size);

    Iterable<Integer> finaAll();

    void saveAll(int[] extras);
}
