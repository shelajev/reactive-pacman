package org.coinen.reactive.pacman.agent.service;

import org.coinen.reactive.pacman.agent.controllers.Direction;
import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core.Game;
import org.coinen.reactive.pacman.agent.model.Decision;
import org.coinen.reactive.pacman.agent.model.Knowledge;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import qlearn.Q_learn;

public class QLearningDecisionService implements DecisionService {
    final KnowledgeRepository knowledgeRepository;
    final Game game;

    public QLearningDecisionService(KnowledgeRepository knowledgeRepository, Game game) {
        this.knowledgeRepository = knowledgeRepository;
        this.game = game;
    }

    @Override
    public Decision decide() {
        final Knowledge last = knowledgeRepository.leastRecent();
        final Knowledge current = knowledgeRepository.mostRecent();

        // Antonio: hack para tomar decisiones solo cuando cambia el estado
        if ((last != null) && Q_learn.normalizedDistance(Q_learn.compareCases(current.gameState, last.gameState)) <= Q_learn.newCaseThreshold) {
            return Decision.UNCHENGED;
        }

        int nextDir;

        if (Q_learn.closeGhost() /*|| rnd < Q_learn.eps*/)
            nextDir = getAction(true);
        else
            nextDir = getAction(false);

        return new Decision(Direction.forIndex(nextDir));
    }

    int getAction(boolean reversal) {
        int[] directions = game.getPossiblePacManDirs(reversal);        //set flag as true to include reversals
        int nextDir;

        if (G.rnd.nextFloat() < Q_learn.eps) {                        // Eps. greedy
            nextDir = directions[G.rnd.nextInt(directions.length)];
            //System.out.println("Random move!!  Now greedy direction is: " + greedyDir);
        } else {
            nextDir = Q_learn.maxQRowDirIndex(directions, knowledgeRepository.mostRecent());
        }

        return nextDir;
    }
}
