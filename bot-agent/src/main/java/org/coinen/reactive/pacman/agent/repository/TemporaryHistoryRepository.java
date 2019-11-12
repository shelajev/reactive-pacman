package org.coinen.reactive.pacman.agent.repository;

import org.coinen.reactive.pacman.agent.model.History;
import org.coinen.reactive.pacman.agent.model.Knowledge;

public interface TemporaryHistoryRepository {

    Knowledge currentKnowledge();

    void update(History history);
}
