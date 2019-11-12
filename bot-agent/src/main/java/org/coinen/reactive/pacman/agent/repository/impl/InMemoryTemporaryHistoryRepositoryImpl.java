package org.coinen.reactive.pacman.agent.repository.impl;

import org.coinen.reactive.pacman.agent.model.History;
import org.coinen.reactive.pacman.agent.model.Knowledge;
import org.coinen.reactive.pacman.agent.repository.TemporaryHistoryRepository;

public class InMemoryTemporaryHistoryRepositoryImpl implements TemporaryHistoryRepository {
    volatile History history;

    @Override
    public Knowledge currentKnowledge() {
        return history.currentKnowledge;
    }

    public void update(History history) {
        this.history = history;
    }
}
