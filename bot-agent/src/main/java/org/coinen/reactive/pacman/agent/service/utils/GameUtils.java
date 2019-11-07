package org.coinen.reactive.pacman.agent.service.utils;

import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core.Game;
import org.coinen.reactive.pacman.agent.model.GameState;
import qlearn.Q_learn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GameUtils {

    public static final int DISTANCE_FAR = 225, DISTANCE_MED = 49, DISTANCE_CLOSE = 4;                                                // Thresholds for distances
    public static final int DISCRETE_FAR = 4, DISCRETE_MED = 3, DISCRETE_CLOSE = 2, DISCRETE_VERY_CLOSE = 1, DISCRETE_WALL = -1;    // Discrete values for distances
    public static final int PILL = 0, GHOST = 1, POWER_PILL = 2, EDIBLE_GHOST = 3;

    private GameUtils() {}


    public static GameState captureState(Game game) {
        return new GameState(-1, GameUtils.getNextPill(game, Game.UP), GameUtils.getNextPill(game, Game.RIGHT), GameUtils.getNextPill(game, Game.DOWN), GameUtils.getNextPill(game, Game.LEFT),
                GameUtils.getNextGhost(game, Game.UP), GameUtils.getNextGhost(game, Game.RIGHT), GameUtils.getNextGhost(game, Game.DOWN), GameUtils.getNextGhost(game, Game.LEFT),
                GameUtils.getNextPowerPill(game, Game.UP), GameUtils.getNextPowerPill(game, Game.RIGHT), GameUtils.getNextPowerPill(game, Game.DOWN), GameUtils.getNextPowerPill(game, Game.LEFT),
                GameUtils.getNextEdibleGhost(game, Game.UP), GameUtils.getNextEdibleGhost(game, Game.RIGHT), GameUtils.getNextEdibleGhost(game, Game.DOWN), GameUtils.getNextEdibleGhost(game, Game.LEFT),
                GameUtils.getNextIntersection(game, Game.UP), GameUtils.getNextIntersection(game, Game.RIGHT), GameUtils.getNextIntersection(game, Game.DOWN), GameUtils.getNextIntersection(game, Game.LEFT));
    }


    /* Returns nearest pill */
    public static int getNearestPill(Game game) {

        int current = game.getCurPacManLoc();

        //get all active pills
        int[] activePills = game.getPillIndicesActive();

        //get all active power pills
        int[] activePowerPills = game.getPowerPillIndicesActive();

        //create a target array that includes all ACTIVE pills and power pills
        int[] targetsArray = new int[activePills.length + activePowerPills.length];

        for (int i = 0; i < activePills.length; i++)
            targetsArray[i] = activePills[i];

        for (int i = 0; i < activePowerPills.length; i++)
            targetsArray[activePills.length + i] = activePowerPills[i];

        return game.getTarget(current, targetsArray, true, G.DM.PATH);
    }

    /* Returns nearest pill in given direction */
    public static int getNearestPillInDirection(Game game, int direction) {

        int[] pillsActive = game.getPillIndicesActive();
        int dist = -1, currBest = Integer.MAX_VALUE, npd = -1;
        int root = game.getCurPacManLoc();
        ArrayList<ArrayList<Integer>> distances;

        if (direction == Game.UP)
            distances = Q_learn.currDistancesUp;
        else if (direction == Game.RIGHT)
            distances = Q_learn.currDistancesRight;
        else if (direction == Game.DOWN)
            distances = Q_learn.currDistancesDown;
        else
            distances = Q_learn.currDistancesLeft;

        for (int p : pillsActive) {
            if (direction == Game.UP)
                dist = distances.get(root).get(p);
            else if (direction == Game.RIGHT)
                dist = distances.get(root).get(p);
            else if (direction == Game.DOWN)
                dist = distances.get(root).get(p);
            else
                dist = distances.get(root).get(p);

            if (dist < currBest && dist >= 0) {
                currBest = dist;
                npd = p;
            }
        }


        return npd;
    }

    /* Returns nearest pill in given direction */
    public static int getNearestPowerPillInDirection(Game game, int direction) {


        int[] pillsActive = game.getPowerPillIndicesActive();
        int dist = -1, currBest = Integer.MAX_VALUE, npd = -1;
        int root = game.getCurPacManLoc();
        ArrayList<ArrayList<Integer>> distances;

        if (direction == Game.UP)
            distances = Q_learn.currDistancesUp;
        else if (direction == Game.RIGHT)
            distances = Q_learn.currDistancesRight;
        else if (direction == Game.DOWN)
            distances = Q_learn.currDistancesDown;
        else
            distances = Q_learn.currDistancesLeft;

        for (int p : pillsActive) {
            if (direction == Game.UP)
                dist = distances.get(root).get(p);
            else if (direction == Game.RIGHT)
                dist = distances.get(root).get(p);
            else if (direction == Game.DOWN)
                dist = distances.get(root).get(p);
            else
                dist = distances.get(root).get(p);

            if (dist < currBest && dist >= 0) {
                currBest = dist;
                npd = p;
            }
        }


        return npd;
    }

    /* Returns nearest ghost in given direction */
    public static int getNearestGhostInDirection(Game game, int direction) {

        int ghostCount = game.getGhostCount();
        int[] ghostLocs = new int[ghostCount];

        int i = 0;
        Set<String> ghostsUuids = game.getGhostsUuids();

        for (String uuid : ghostsUuids) {
            ghostLocs[i++] = !game.isEdible(uuid) ? game.getCurGhostLoc(uuid) : -1;
        }

        int dist = Integer.MAX_VALUE, currBest = Integer.MAX_VALUE, npd = -1;
        int root = game.getCurPacManLoc();
        ArrayList<ArrayList<Integer>> distances;

        if (direction == Game.UP)
            distances = Q_learn.currDistancesUp;
        else if (direction == Game.RIGHT)
            distances = Q_learn.currDistancesRight;
        else if (direction == Game.DOWN)
            distances = Q_learn.currDistancesDown;
        else
            distances = Q_learn.currDistancesLeft;

        for (int p : ghostLocs) {
            if (p >= 0 && p < distances.size() - 1)
                if (direction == Game.UP)
                    dist = distances.get(root).get(p);
                else if (direction == Game.RIGHT)
                    dist = distances.get(root).get(p);
                else if (direction == Game.DOWN)
                    dist = distances.get(root).get(p);
                else
                    dist = distances.get(root).get(p);

            if (dist < currBest && dist != -1) {
                currBest = dist;
                npd = p;
            }
        }


        return npd;
    }

    /* Returns nearest edible ghost in given direction */
    public static int getNearestEdibleGhostInDirection(Game game, int direction) {

        int dist = Integer.MAX_VALUE, currBest = Integer.MAX_VALUE, npd = -1;
        int root = game.getCurPacManLoc();
        ArrayList<ArrayList<Integer>> distances;
        if (direction == Game.UP)
            distances = Q_learn.currDistancesUp;
        else if (direction == Game.RIGHT)
            distances = Q_learn.currDistancesRight;
        else if (direction == Game.DOWN)
            distances = Q_learn.currDistancesDown;
        else
            distances = Q_learn.currDistancesLeft;

        Set<String> ghostsUuids = game.getGhostsUuids();

        for (String uuid : ghostsUuids) {
            if (game.isEdible(uuid)) {
                int p = game.getCurGhostLoc(uuid);
                if (p >= 0 && p < (distances.size() - 1))
                    if (direction == Game.UP)
                        dist = distances.get(root).get(p);
                    else if (direction == Game.RIGHT)
                        dist = distances.get(root).get(p);
                    else if (direction == Game.DOWN)
                        dist = distances.get(root).get(p);
                    else
                        dist = distances.get(root).get(p);

                if (dist < currBest && dist >= 0) {
                    currBest = dist;
                    npd = p;
                }
            }
        }


        return npd;
    }

    // Returns nearest power pill in any direction
    public static int getNearestPowerPill(Game game) {

        int current = game.getCurPacManLoc();
        //get all active power pills
        int[] activePowerPills = game.getPowerPillIndicesActive();
        int result = current;


        if (activePowerPills.length > 0)
            result = game.getTarget(current, activePowerPills, true, G.DM.PATH);

        return result;
    }

    /* Returns the number of ghosts that are in a node close to pacman*/
//    public static int nearGhosts(Game game) {
//
//        int result = 0, distance;
//
//        for (int i = 0; i < 4; i++) {
//            distance = game.getPathDistance(game.getCurGhostLoc(i), game.getCurPacManLoc());
//            if (distance < 5 && distance != -1) result++;
//        }
//
//        return result;
//    }

    /* Returns true if there is at least an edible ghost*/
//    public static boolean edibleGhost(Game game) {
//        for (int i = 1; i < 4; i++) {
//            if (game.isEdible(i)) return true;
//        }
//
//        return false;
//    }

    // This function transforms input distance into discrete and more useful values
    public static int discretizeDistance(int distance) {

        if (distance >= 0 && distance < DISTANCE_CLOSE) return DISCRETE_VERY_CLOSE;
        else if (distance >= DISTANCE_CLOSE && distance < DISTANCE_MED) return DISCRETE_CLOSE;
        else if (distance >= DISTANCE_MED && distance < DISTANCE_FAR) return DISCRETE_MED;
        else if (distance >= DISTANCE_FAR) return DISCRETE_FAR;
        else return DISCRETE_WALL;
    }

    /* Returns distance to next pill in given direction*/
    public static int getNextPill(Game game, int direction) {
        int[] neigh = game.getPacManNeighbours();
        int curr = game.getCurPacManLoc(), next = -1;


        int npd = getNearestPillInDirection(game, direction);
        if (npd != -1) {
            int dist = Q_learn.getDistance(direction, curr, npd);
            return discretizeDistance(dist);
        }
        return -1;

    }

    /* Returns distance to next power pill in given direction*/
    public static int getNextPowerPill(Game game, int direction) {
        int[] neigh = game.getPacManNeighbours();
        int curr = game.getCurPacManLoc(), next = -1;

        int npd = getNearestPowerPillInDirection(game, direction);
        if (npd != -1) {
            int dist = Q_learn.getDistance(direction, curr, npd);
            return discretizeDistance(dist);
        }
        return -1;
    }

    /* Returns distance to next ghost pill in given direction*/
    public static int getNextGhost(Game game, int direction) {
        int[] neigh = game.getPacManNeighbours();
        int curr = game.getCurPacManLoc(), next = -1, distance = 0;


        int npd = getNearestGhostInDirection(game, direction);

        if (npd != -1) {
            int dist = Q_learn.getDistance(direction, curr, npd);

            return discretizeDistance(dist);
        }
        return -1;
    }

    /* Returns distance to next power edible ghost in given direction*/
    public static int getNextEdibleGhost(Game game, int direction) {
        int[] neigh = game.getPacManNeighbours();
        int curr = game.getCurPacManLoc(), next = -1, distance = 0;

        int npd = getNearestEdibleGhostInDirection(game, direction);
        if (npd != -1) {
            int dist = Q_learn.getDistance(direction, curr, npd);
            return discretizeDistance(dist);
        }
        return -1;
    }

    /* Returns distance to next intersection in given direction*/
    public static int getNextIntersection(Game game, int direction) {
        int[] neigh = game.getPacManNeighbours();
        int curr = game.getCurPacManLoc(), next, distance = 0;


        next = game.getNeighbour(curr, direction);
        while (next != -1) {
            if (game.isJunction(next)) return discretizeDistance(distance + 1); /*return distance + 1*/

            curr = next;
            next = game.getNeighbour(curr, direction);
            distance++;
        }

        return -1;
    }

    /*Returns direction of closest pill*/
    public static int getClosestPillDir(Game game) {
        int curr = game.getCurPacManLoc(), next = -1;


        int dir = -1, currMin = Integer.MAX_VALUE;
        int dist = Integer.MAX_VALUE;

        for (int direction = 0; direction < 4; direction++) {
            int npd = getNearestPillInDirection(game, direction);
            if (npd != -1) {
                dist = Q_learn.getDistance(direction, curr, npd);
                if (dist < currMin && dist != -1) {
                    dir = direction;
                    currMin = dist;
                }
            }
        }


        return dir;

    }

    /*Returns direction of closest power pill*/
    public static int getClosestPowerPillDir(Game game) {
        int curr = game.getCurPacManLoc(), next = -1;
        int dist = Integer.MAX_VALUE;

        int dir = -1, currMin = Integer.MAX_VALUE;

        for (int direction = 0; direction < 4; direction++) {
            int npd = getNearestPowerPillInDirection(game, direction);
            if (npd != -1) {
                dist = Q_learn.getDistance(direction, curr, npd);
                if (dist < currMin && dist != -1) {
                    dir = direction;
                    currMin = dist;
                }
            }
        }


        return dir;

    }

    /*Returns direction of closest ghost*/
    public static int getClosestGhostDir(Game game) {
        int curr = game.getCurPacManLoc(), next = -1;


        int dir = -1, currMin = Integer.MAX_VALUE;

        for (int direction = 0; direction < 4; direction++) {
            int npd = getNearestGhostInDirection(game, direction);
            if (npd != -1) {
                int dist = Q_learn.getDistance(direction, curr, npd);
                if (dist < currMin && dist != -1) {
                    dir = direction;
                    currMin = dist;
                }
            }
        }


        return dir;

    }

    /*Returns direction of closest edible ghost*/
    public static int getClosestEdibleGhostDir(Game game) {
        int curr = game.getCurPacManLoc(), next = -1;


        int dir = -1, currMin = Integer.MAX_VALUE;

        for (int direction = 0; direction < 4; direction++) {
            int npd = getNearestEdibleGhostInDirection(game, direction);
            if (npd != -1) {
                int dist = Q_learn.getDistance(direction, curr, npd);
                if (dist < currMin && dist != -1) {
                    dir = direction;
                    currMin = dist;
                }
            }
        }


        return dir;

    }

    /*Returns 1 if there is a non-edible ghost close to pacman, 0 otherwise*/
//    public static int closeGhostInDirection(Game game, int direction) {
//        int curr = game.getCurPacManLoc(), next = -1, distance = 0;
//
//        next = game.getNeighbour(curr, direction);
//
//
//        next = game.getNeighbour(curr, direction);
//
//        if (next == -1) return -1;
//        while (next != -1 && distance < 4) {
//            for (int i = 0; i < 4; i++)
//                if (game.getCurGhostLoc(i) == next && !game.isEdible(i)) return 1;
//
//
//            curr = next;
//            next = game.getNeighbour(curr, direction);
//            distance++;
//        }
//
//
//        return 0;
//
//    }

    /*Returns true if d is one of the possible directions for pacman to take, false otherwise*/
    public static boolean possibleDir(Game game, int d) {
        int[] directions = game.getPossiblePacManDirs(true);

        for (int dir : directions) {
            if (dir == d) return true;
        }

        return false;
    }

    /*Returns closest element in given direction*/
    public static int getClosestElement(Game game, int direction) {

        List<Integer> distances = new ArrayList<Integer>();
        int dist = Integer.MAX_VALUE, currResult = -1, index = -1;

        distances.add(getNextPill(game, direction));
        distances.add(getNextPowerPill(game, direction));
        distances.add(getNextGhost(game, direction));
        distances.add(getNextEdibleGhost(game, direction));

        for (int i = 0; i < 4; i++) {
            int d = distances.get(i);
            if (d < dist) {
                dist = d;
                index = i;
            }

        }

        switch (index) {
            case 0:
                return PILL;
            case 1:
                return GHOST;
            case 2:
                return POWER_PILL;
            case 3:
                return EDIBLE_GHOST;
            default:
                return -1;
        }

    }
}
