package qlearn;

import org.coinen.reactive.pacman.agent.controllers.pacman.PacManHijackController;
import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core.Game;
import org.coinen.reactive.pacman.agent.model.GameState;


/* This class implements the learning agent for PacMan game. It chooses a direction every time it gets to an intersection, and also if it
 * notice a near ghost.
 * */
public class QLearnDirPacMan extends PacManHijackController {

    GameState lastState = null;

    @Override
    public void tick(Game game, long timeDue) {
        // Antonio: hack para tomar decisiones solo cuando cambia el estado
        if ((lastState != null) && Q_learn.normalizedDistance(Q_learn.compareCases(Q_learn.currState, lastState)) <= Q_learn.newCaseThreshold) {
            return;
        } else {
            lastState = Q_learn.currState;
        }


        int nextDir;
        float rnd = G.rnd.nextFloat();

        if (Q_learn.closeGhost() /*|| rnd < Q_learn.eps*/)
            nextDir = getAction(game, true);
        else
            nextDir = getAction(game, false);

        pacman.set(nextDir);
        Q_learn.currAction = nextDir;
        Q_learn.reward = 0;
    }

    public int getAction(Game game, boolean reversal) {
        int[] directions = game.getPossiblePacManDirs(reversal);        //set flag as true to include reversals
        int nextDir;

        if (G.rnd.nextFloat() < Q_learn.eps) {                        // Eps. greedy
            nextDir = directions[G.rnd.nextInt(directions.length)];
            //System.out.println("Random move!!  Now greedy direction is: " + greedyDir);
        } else {
            nextDir = Q_learn.maxQRowDirIndex(directions, Q_learn.currState.index);
        }

        return nextDir;
    }

    private void printDir(int index) {

        switch (index) {
            case 0:
                System.out.println("UP");
                break;
            case 1:
                System.out.println("RIGHT");
                break;
            case 2:
                System.out.println("DOWN");
                break;
            case 3:
                System.out.println("LEFT");
                break;
            default:
                break;
        }

    }

}
