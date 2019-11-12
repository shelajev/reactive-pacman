package org.coinen.reactive.pacman.agent.service.impl;

import org.coinen.reactive.pacman.agent.controllers.Direction;
import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core.Game;
import org.coinen.reactive.pacman.agent.core._G_;
import org.coinen.reactive.pacman.agent.model.Decision;
import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.model.Knowledge;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import org.coinen.reactive.pacman.agent.repository.TemporaryHistoryRepository;
import org.coinen.reactive.pacman.agent.service.DecisionService;
import qlearn.Q_learn;
import reactor.core.publisher.Flux;

public class QLearningDecisionService implements DecisionService {
    final TemporaryHistoryRepository temporaryHistoryRepository;
    final KnowledgeRepository knowledgeRepository;
    final Game game;

    public QLearningDecisionService(TemporaryHistoryRepository temporaryHistoryRepository, KnowledgeRepository knowledgeRepository, Game game) {
        this.temporaryHistoryRepository = temporaryHistoryRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.game = game;
    }

    @Override
    public Flux<Decision> decide() {
        return Flux.<Decision, Decision>generate(() -> null, (last, sink) -> {
            final Knowledge current = temporaryHistoryRepository.currentKnowledge();

            int nextDir;

            if (Q_learn.closeGhost(current.gameState) || last == null /*|| rnd < Q_learn.eps*/)
                nextDir = getAction(current);
            else
                nextDir = getAction(current, last);


            Decision decision = new Decision(Direction.forIndex(nextDir));
            sink.next(decision);
            return decision;
        });
    }

    int getAction(Knowledge knowledge) {
        int[] directions = game.getPossiblePacManDirs();        //set flag as true to include reversals
        int nextDir;

        float eps = (0.5f / ((float) knowledgeRepository.size() / _G_.maze.graph.length)) / _G_.maze.graph.length;

        if (G.rnd.nextFloat() < eps) {                        // Eps. greedy
            nextDir = directions[G.rnd.nextInt(directions.length)];
            //System.out.println("Random move!!  Now greedy direction is: " + greedyDir);
        } else {
            nextDir = Q_learn.maxQRowDirIndex(directions, knowledge);
        }

        return nextDir;
    }


    int getAction(Knowledge knowledge, Decision decision) {
        int[] directions = game.getPossiblePacManDirs(decision);        //set flag as true to include reversals
        int nextDir;

        float eps = (0.5f / ((float) knowledgeRepository.size() / _G_.maze.graph.length)) / _G_.maze.graph.length;

        if (G.rnd.nextFloat() < eps) {                        // Eps. greedy
            nextDir = directions[G.rnd.nextInt(directions.length)];
            //System.out.println("Random move!!  Now greedy direction is: " + greedyDir);
        } else {
            nextDir = Q_learn.maxQRowDirIndex(directions, knowledge);
        }

        return nextDir;
    }
}
