/*
 * Implementation of "Ms Pac-Man" for the "Ms Pac-Man versus Ghost Team Competition", brought
 * to you by Philipp Rohlfshagen, David Robles and Simon Lucas of the University of Essex.
 *
 * www.pacman-vs-ghosts.net
 *
 * Code written by Philipp Rohlfshagen, based on earlier implementations of the game by
 * Simon Lucas and David Robles.
 *
 * You may use and distribute this code freely for non-commercial purposes. This notice
 * needs to be included in all distributions. Deviations from the original should be
 * clearly documented. We welcome any comments and suggestions regarding the code.
 */
package org.coinen.reactive.pacman.agent.core;

import org.coinen.reactive.pacman.agent.controllers.Direction;
import org.coinen.reactive.pacman.agent.controllers.pacman.PacManAction;
import org.coinen.pacman.Tile;
import org.coinen.reactive.pacman.agent.model.Decision;
import qlearn.Q_learn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/*
 * Simple implementation of Ms Pac-Man. The class Game contains all code relating to the
 * game; the class GameView displays the game. Controllers must implement PacManController
 * and GhostController respectively. The game may be executed using Exec.
 */
public class G implements Game {
    public static Random rnd = new Random();


    protected int remainingLevels;

    //Static stuff (mazes are immutable - hence static)
    public static Maze maze;

    //Variables (game state):
    public BitSet pills, powerPills;
    //level-specific
    protected int curMaze, totLevel, levelTime, totalTime, score, ghostEatMultiplier;
    protected volatile boolean gameOver;
    //pac-man-specific
    protected int curPacManLoc, lastPacManDir, livesRemaining;
    protected boolean extraLife;
    //ghosts-specific
    public volatile ConcurrentMap<String, Ghost> curGhosts;
    private volatile boolean isPowerUpEnabled;

    /////////////////////////////////////////////////////////////////////////////
    /////////////////  Constructors and Initialisers   //////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    //Constructor
    protected G() {
    }

    //loads the mazes and store them
    protected void init() {
//        for (int i = 0; i < mazes.length; i++)
//            if (mazes[i] == null)
//                mazes[i] = new Maze(i);
    }

    //Creates an exact copy of the game
    public Game copy() {
        G copy = new G();
        copy.pills = (BitSet) pills.clone();
        copy.powerPills = (BitSet) powerPills.clone();
        copy.curMaze = curMaze;
        copy.totLevel = totLevel;
        copy.levelTime = levelTime;
        copy.totalTime = totalTime;
        copy.score = score;
        copy.ghostEatMultiplier = ghostEatMultiplier;
        copy.gameOver = gameOver;
        copy.curPacManLoc = curPacManLoc;
        copy.lastPacManDir = lastPacManDir;
        copy.livesRemaining = livesRemaining;
        copy.extraLife = extraLife;
        copy.curGhosts = new ConcurrentHashMap<>(curGhosts);

        return copy;
    }

    //If pac-man has been eaten or a new level has been reached
    protected void reset(boolean newLevel) {
        if (newLevel) {
            if (remainingLevels > 0) {
                --remainingLevels;
                if (remainingLevels <= 0) {
                    gameOver = true;
                    return;
                }
            }

            curMaze = (curMaze + 1) % NUM_MAZES;
            totLevel++;
            Q_learn.currLevels++;
            levelTime = 0;
            pills = new BitSet(getNumberPills());
            pills.set(0, getNumberPills());
            powerPills = new BitSet(getNumberPowerPills());
            powerPills.set(0, getNumberPowerPills());
        }

        Q_learn.changeDistances(curMaze);

        curPacManLoc = getInitialPacPosition();
        lastPacManDir = INITIAL_PAC_DIR;

        curGhosts = new ConcurrentHashMap<>();

        ghostEatMultiplier = 1;

    }

    // Remove 'number' of pills from the maze
    protected void decimatePills(int number) {
        if (number == pills.length()) {
            pills.clear();
        } else {
            List<Integer> pillNodeIndices = new ArrayList<>();
            Node[] graph = maze.graph;
            for (int i = 0; i < graph.length; ++i) {
                if (graph[i].pillIndex >= 0) {
                    pillNodeIndices.add(i);
                }
            }
            while (number > 0) {
                int startNodePillIndex = pillNodeIndices.get(G.rnd.nextInt(pillNodeIndices.size()));
                List<Integer> nodeIndices = new ArrayList<>();
                Set<Integer> closedIndices = new HashSet<>();
                nodeIndices.add(startNodePillIndex);
                while (number > 0 && nodeIndices.size() > 0) {
                    // CLEAR PILL
                    int nodeIndex = nodeIndices.remove(0);
                    int pillIndex = getPillIndex(nodeIndex);
                    pillNodeIndices.remove((Object) nodeIndex);
                    closedIndices.add(nodeIndex);

                    if (pillIndex >= 0 && pills.get(pillIndex)) {
                        pills.clear(pillIndex);
                        --number;
                    }

                    // CHECK NEIGHBOURS
                    int[] neighbours = new int[4];
                    int numNeighbours = 0;
                    for (Direction dir : Direction.arrows()) {
                        int nextNode = getNeighbour(nodeIndex, dir.index);
                        if (nextNode >= 0) {
                            neighbours[dir.index] = nextNode;
                            ++numNeighbours;
                        } else {
                            neighbours[dir.index] = -1;
                        }
                    }
                    if (numNeighbours == 2) {
                        // CORRIDOR
                        for (int neighbour : neighbours) {
                            if (neighbour >= 0 && !closedIndices.contains(neighbour)) {
                                nodeIndices.add(neighbour);
                            }
                        }
                    }
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////  Game Play   //////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    //Central method that advances the game state
    public int[] advanceGame(PacManAction pacMan/*, GhostsActions ghosts*/) {
        updatePacMan(pacMan);          //move pac-man
        eatPill();                          //eat a pill
        boolean reverse = eatPowerPill();      //eat a power pill
//        if (ghosts != null) {
//            updateGhosts(ghosts, reverse);    //move ghosts
//        }

        //This is primarily done for the replays as reset (as possibly called by feast()) sets the
        //last directions to the initial ones, not the ones taken
//        int[] replayStep = {lastPacManDir, lastGhostDirs.ge[0], lastGhostDirs[1], lastGhostDirs[2], lastGhostDirs[3], curPacManLoc, curGhostLocs[0], curGhostLocs[1], curGhostLocs[2], curGhostLocs[3]};

        feast();                            //ghosts eat pac-man or vice versa



        if (!extraLife && score >= EXTRA_LIFE_SCORE)    //award 1 extra life at 10000 points
        {
            extraLife = true;
            livesRemaining++;
        }

        totalTime++;
        levelTime++;
        checkLevelState();    //check if level/game is over

        return new int[0];
    }

    public int advanceGame(Decision pacMan/*, GhostsActions ghosts*/) {
        int gain = 0;
        updatePacMan(pacMan);          //move pac-man
        gain += eatPill();                          //eat a pill
        boolean reverse = eatPowerPill();      //eat a power pill

        if (reverse) {
            gain += POWER_PILL;
        }
//        if (ghosts != null) {
//            updateGhosts(ghosts, reverse);    //move ghosts
//        }

        //This is primarily done for the replays as reset (as possibly called by feast()) sets the
        //last directions to the initial ones, not the ones taken
//        int[] replayStep = {lastPacManDir, lastGhostDirs.ge[0], lastGhostDirs[1], lastGhostDirs[2], lastGhostDirs[3], curPacManLoc, curGhostLocs[0], curGhostLocs[1], curGhostLocs[2], curGhostLocs[3]};

        int feastResult = feast();                            //ghosts eat pac-man or vice versa

        if (feastResult == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        } else {
            gain += feastResult;
        }

        return gain;
    }

    //Updates the location of Ms Pac-Man
    protected void updatePacMan(PacManAction pacMan) {
        int direction = checkPacManDir(pacMan.get().index);
        lastPacManDir = direction;
        curPacManLoc = getNeighbour(curPacManLoc, direction);
    }

    //Updates the location of Ms Pac-Man
    protected void updatePacMan(Decision pacMan) {
        int direction;
        if (pacMan == Decision.UNCHANGED) {
            direction = lastPacManDir;
        } else {
            direction = checkPacManDir(pacMan.getDirection().index);
            lastPacManDir = direction;
        }
        curPacManLoc = getNeighbour(curPacManLoc, direction);
    }

    //Checks the direction supplied by the controller and substitutes for a legal one if necessary
    protected int checkPacManDir(int direction) {
        int[] neighbours = getPacManNeighbours();

        if ((direction > 3 || direction < 0 || neighbours[direction] == -1) && (lastPacManDir > 3 || lastPacManDir < 0 || neighbours[lastPacManDir] == -1))
            return 4;

        if (direction < 0 || direction > 3)
            direction = lastPacManDir;

        if (neighbours[direction] == -1)
            if (neighbours[lastPacManDir] != -1)
                direction = lastPacManDir;
            else {
                int[] options = getPossiblePacManDirs();
                direction = options[G.rnd.nextInt(options.length)];
            }

        return direction;
    }

    //Updates the locations of the ghosts
    protected void updateGhosts(/*GhostsActions ghosts,*/ boolean reverse) {
//        int[] directions = new int[4];
//        for (int i = 0; i < ghosts.ghostCount; ++i) {
//            directions[i] = ghosts.actions[i].get().index;
//        }
//
//        if (directions == null) {
//            directions = Arrays.copyOf(lastGhostDirs, lastGhostDirs.length);
//        }
//
//        for (int i = 0; i < ghosts.ghostCount; i++) {
//            if (lairTimes[i] == 0) {
//                if (reverse) {
//                    lastGhostDirs[i] = getReverse(lastGhostDirs[i]);
//                    curGhostLocs[i] = getNeighbour(curGhostLocs[i], lastGhostDirs[i]);
//                } else if (edibleTimes[i] == 0 || edibleTimes[i] % GHOST_SPEED_REDUCTION != 0) {
//                    directions[i] = checkGhostDir(i, directions[i]);
//                    lastGhostDirs[i] = directions[i];
//                    curGhostLocs[i] = getNeighbour(curGhostLocs[i], directions[i]);
//                }
//            }
//        }
    }

    @Override
    public int getGhostCount() {
        return curGhosts.size();
    }

    //Checks the directions supplied by the controller and substitutes for a legal ones if necessary
//    protected int checkGhostDir(String whichGhost, int direction) {
//        if (direction < 0 || direction > 3)
//            direction = curGhosts.get(whichGhost).direction;
//
//        int[] neighbours = getGhostNeighbours(whichGhost);
//
//        if (neighbours[direction] == -1) {
//            if (neighbours[lastGhostDirs[whichGhost]] != -1)
//                direction = lastGhostDirs[whichGhost];
//            else {
//                int[] options = getPossibleGhostDirs(whichGhost);
//                direction = options[G.rnd.nextInt(options.length)];
//            }
//        }
//
//        return direction;
//    }

    //Eats a pill
    protected int eatPill() {
        int pillIndex = getPillIndex(curPacManLoc);

        if (pillIndex >= 0 && pills.get(pillIndex)) {
            score += PILL;
            synchronized (pills) {
                pills.clear(pillIndex);
            }
            return PILL;
        }

        return 0;
    }

    long powerStopTick;
    //Eats a power pill - turns ghosts edible (blue)
    protected boolean eatPowerPill() {
        boolean reverse = false;
        int powerPillIndex = getPowerPillIndex(curPacManLoc);

        if (isPowerUpEnabled && System.nanoTime() > powerStopTick) {
            isPowerUpEnabled = false;
        }

        if (powerPillIndex >= 0 && powerPills.get(powerPillIndex)) {
            score += POWER_PILL;
            ghostEatMultiplier = 1;
            synchronized (powerPills) {
                powerPills.clear(powerPillIndex);
            }


            //This ensures that only ghosts outside the lair (i.e., inside the maze) turn edible
            powerStopTick = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            isPowerUpEnabled = true;
//            for (int i = 0; i < NUM_GHOSTS; i++)
//                if (lairTimes[i] == 0)
//                    edibleTimes[i] = newEdibleTime;
//                else
//                    edibleTimes[i] = 0;

            //This turns all ghosts edible, independent on whether they are in the lair or not
//			Arrays.fill(edibleTimes,(int)(G.EDIBLE_TIME*(Math.pow(G.EDIBLE_TIME_REDUCTION,totLevel))));						

            reverse = true;
        } else if (levelTime > 1 && G.rnd.nextDouble() < GHOST_REVERSAL) //random ghost reversal
            reverse = true;

        return reverse;
    }

    //This is where the characters of the game eat one another if possible
    public int feast() {
        if (gameOver) {
            return Integer.MIN_VALUE;
        }
        int gain = 0;
        for (var ghost : curGhosts.values()) {
            int distance = getPathDistance(curPacManLoc, ghost.location);

            if (distance <= EAT_DISTANCE && distance != -1) {
                if (isPowerUpEnabled())                                    //pac-man eats ghost
                {
                    gain += GHOST_EAT_SCORE * ghostEatMultiplier;
                    score += GHOST_EAT_SCORE * ghostEatMultiplier;
                    ghostEatMultiplier *= 2;
                } else                                                    //ghost eats pac-man
                {
                    kill();
                    return Integer.MIN_VALUE;
                }
            }
        }

        return gain;
    }

    public void kill() {
        synchronized (Q_learn.class) {
            Q_learn.eaten = true;
            Q_learn.totalReward = 0;


            Q_learn.justEaten = true;
            Q_learn.prevTime = 0;
            Q_learn.times.add(totalTime);
            gameOver = true;
        }
    }

    //Checks the state of the level/game and advances to the next level or terminates the game
    protected void checkLevelState() {
        //if all pills have been eaten or the time is up...
        if ((pills.isEmpty() && powerPills.isEmpty()) || levelTime >= LEVEL_LIMIT) {

            Q_learn.times.add(totalTime);

            //award any remaining pills to Ms Pac-Man
            score += PILL * pills.cardinality() + POWER_PILL * powerPills.cardinality();

            //put a cap on the total number of levels played
            if (totLevel + 1 == MAX_LEVELS) {
                gameOver = true;
                return;
            } else
                reset(true);
        }
        //if(!Q_learn.rewardFlag)Q_learn.reward = -1;

        //Q_learn.rewardFlag = false;
    }

    /////////////////////////////////////////////////////////////////////////////
    ///////////////////////////  Getter Methods  ////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    //Returns the reverse of the direction supplied
    public int getReverse(int direction) {
        switch (direction) {
            case 0:
                return 2;
            case 1:
                return 3;
            case 2:
                return 0;
            case 3:
                return 1;
        }

        return 4;
    }

    //Whether the game is over or not
    public boolean gameOver() {
        return gameOver;
    }

    //Whether the pill specified is still there
    public boolean checkPill(int nodeIndex) {
        return pills.get(nodeIndex);
    }

    //Whether the power pill specified is still there
    public boolean checkPowerPill(int nodeIndex) {
        return powerPills.get(nodeIndex);
    }

    //Returns the neighbours of the node at which Ms Pac-Man currently resides
    public int[] getPacManNeighbours() {
        return Arrays.copyOf(maze.graph[curPacManLoc].neighbours, maze.graph[curPacManLoc].neighbours.length);
    }

    //Returns the neighbours of the node at which the specified ghost currently resides. NOTE: since ghosts are not allowed to reverse, that
    //neighbour is filtered out. Alternatively use: getNeighbour(), given curGhostLoc[-] for all directions
    public int[] getGhostNeighbours(String whichGhost) {
        Ghost ghost = curGhosts.get(whichGhost);
        int[] neighboursToCopy = maze.graph[ghost.location].neighbours;
        int[] neighbours = Arrays.copyOf(neighboursToCopy, neighboursToCopy.length);
        neighbours[getReverse(ghost.direction)] = -1;

        return neighbours;
    }

    @Override
    public Set<String> getGhostsUuids() {
        return curGhosts.keySet();
    }

    @Override
    public boolean isPowerUpEnabled() {
        return isPowerUpEnabled;
    }

    //The current level
    public int getCurLevel() {
        return totLevel;
    }

    //The current maze (1-4)
    public int getCurMaze() {
        return curMaze;
    }

    //Current node index of Ms Pac-Man
    public int getCurPacManLoc() {
        return curPacManLoc;
    }

    //Current node index of Ms Pac-Man
    public int getCurPacManDir() {
        return lastPacManDir;
    }

    //Lives that remain for Ms Pac-Man
    public int getLivesRemaining() {
        return livesRemaining;
    }

    //Current node at which the specified ghost resides
    public int getCurGhostLoc(String whichGhost) {
        return curGhosts.get(whichGhost).location;
    }

    //Current direction of the specified ghost
    public int getCurGhostDir(String whichGhost) {
        return curGhosts.get(whichGhost).direction;
    }

    //Simpler check to see if a ghost is edible
    public boolean isEdible(String whichGhost) {
        return isPowerUpEnabled();
    }

    //Returns the score of the game
    public int getScore() {
        return score;
    }

    //Returns the time of the current level (important with respect to LEVEL_LIMIT)
    public int getLevelTime() {
        return levelTime;
    }

    //Total time the game has been played for (at most LEVEL_LIMIT*MAX_LEVELS)
    public int getTotalTime() {
        return totalTime;
    }

    //Total number of pills in the maze
    public int getNumberPills() {
        return maze.pillIndices.length;
    }

    //Total number of power pills in the maze
    public int getNumberPowerPills() {
        return maze.powerPillIndices.length;
    }

    //Returns name of maze: A, B, C, D
    public String getName() {
        return maze.name;
    }

    //Returns the starting position of Ms PacMan
    public int getInitialPacPosition() {
        return maze.initialPacPosition;
    }

    //Total number of nodes in the graph (i.e., those with pills, power pills and those that are empty)
    public int getNumberOfNodes() {
        return maze.graph.length;
    }

    //Returns the x coordinate of the specified node
    public int getX(int index) {
        return maze.graph[index].x;
    }

    //Returns the y coordinate of the specified node
    public int getY(int index) {
        return maze.graph[index].y;
    }

    //Returns the pill index of the node. If it is -1, the node has no pill. Otherwise one can
    //use the bitset to check whether the pill has already been eaten
    public int getPillIndex(int nodeIndex) {
        return maze.graph[nodeIndex].pillIndex;
    }

    //Returns the power pill index of the node. If it is -1, the node has no pill. Otherwise one
    //can use the bitset to check whether the pill has already been eaten
    public int getPowerPillIndex(int nodeIndex) {
        return maze.graph[nodeIndex].powerPillIndex;
    }

    //Returns the neighbour of node index that corresponds to direction. In the case of neutral, the
    //same node index is returned
    public int getNeighbour(int nodeIndex, int direction) {
        if (direction < 0 || direction > 3)//this takes care of "neutral"
            return nodeIndex;
        else
            return maze.graph[nodeIndex].neighbours[direction];
    }

    //Returns the indices to all the nodes that have pills
    public int[] getPillIndices() {
        return Arrays.copyOf(maze.pillIndices, maze.pillIndices.length);
    }

    //Returns the indices to all the nodes that have power pills
    public int[] getPowerPillIndices() {
        return Arrays.copyOf(maze.powerPillIndices, maze.powerPillIndices.length);
    }

    //Returns the indices to all the nodes that are junctions
    public int[] getJunctionIndices() {
        return Arrays.copyOf(maze.junctionIndices, maze.junctionIndices.length);
    }

    // Checks if a node is a corner
    public boolean isCorner(int nodeIndex) {
        if ((getNeighbour(nodeIndex, UP) != -1 && getNeighbour(nodeIndex, RIGHT) != -1 && getNeighbour(nodeIndex, DOWN) == -1 && getNeighbour(nodeIndex, LEFT) == -1) ||   // Bottom left corner
                (getNeighbour(nodeIndex, UP) != -1 && getNeighbour(nodeIndex, RIGHT) == -1 && getNeighbour(nodeIndex, DOWN) == -1 && getNeighbour(nodeIndex, LEFT) != -1) ||    // Bottom right corner
                (getNeighbour(nodeIndex, UP) == -1 && getNeighbour(nodeIndex, RIGHT) != -1 && getNeighbour(nodeIndex, DOWN) != -1 && getNeighbour(nodeIndex, LEFT) == -1) ||    // Top left corner
                (getNeighbour(nodeIndex, UP) == -1 && getNeighbour(nodeIndex, RIGHT) == -1 && getNeighbour(nodeIndex, DOWN) != -1 && getNeighbour(nodeIndex, LEFT) != -1))    // Top right corner
            return true;
        else return false;

    }

    //Checks of a node is a junction
    public boolean isJunction(int nodeIndex) {
        return maze.graph[nodeIndex].numNeighbours > 2;
    }

    //returns the score awarded for the next ghost to be eaten
    public int getNextEdibleGhostScore() {
        return GHOST_EAT_SCORE * ghostEatMultiplier;
    }

    //returns the number of pills still in the maze
    public int getNumActivePills() {
        return pills.cardinality();
    }

    //returns the number of power pills still in the maze
    public int getNumActivePowerPills() {
        return powerPills.cardinality();
    }

    //returns the indices of all active pills in the maze
    public int[] getPillIndicesActive() {
        int[] indices = new int[pills.cardinality()];

        int index = 0;

        for (int i = 0; i < maze.pillIndices.length; i++)
            if (pills.get(i))
                indices[index++] = maze.pillIndices[i];

        return indices;
    }

    //returns the indices of all active power pills in the maze
    public int[] getPowerPillIndicesActive() {
        int[] indices = new int[powerPills.cardinality()];

        int index = 0;

        for (int i = 0; i < maze.powerPillIndices.length; i++)
            if (powerPills.get(i))
                indices[index++] = maze.powerPillIndices[i];

        return indices;
    }

    //Returns the number of neighbours of a node: 2, 3 or 4. Exception: lair, which has no neighbours
    public int getNumNeighbours(int nodeIndex) {
        return maze.graph[nodeIndex].numNeighbours;
    }

    //Returns the actual directions Ms Pac-Man can take
    public int[] getPossiblePacManDirs() {
        return getPossibleDirs(curPacManLoc, lastPacManDir, true);
    }
    //Returns the actual directions Ms Pac-Man can take
    public int[] getPossiblePacManDirs(Decision lastDecision) {
        return getPossibleDirs(curPacManLoc, lastDecision.getDirection().index);
    }

    private int[] getPossibleDirs(int curLoc, int directionToExclude) {
        int numNeighbours = maze.graph[curLoc].numNeighbours;

        if (numNeighbours == 0)
            return new int[0];

        int[] nodes = maze.graph[curLoc].neighbours;
        int[] directions;

        if (numNeighbours == 1)
            directions = new int[numNeighbours];
        else
            directions = new int[numNeighbours - 1];

        int index = 0;

        for (int i = 0; i < nodes.length; i++)
            if (nodes[i] != -1) {
                if (numNeighbours == 1)
                    directions[index++] = i;
                else if (i != getReverse(directionToExclude))
                    try {
                        directions[index++] = i;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
            }

        return directions;
    }

    //Computes the directions to be taken given the current location
    private int[] getPossibleDirs(int curLoc, int curDir, boolean includeReverse) {
        int numNeighbours = maze.graph[curLoc].numNeighbours;

        if (numNeighbours == 0)
            return new int[0];

        int[] nodes = maze.graph[curLoc].neighbours;
        int[] directions;

        if (includeReverse || (curDir < 0 || curDir > 3) || numNeighbours == 1)
            directions = new int[numNeighbours];
        else
            directions = new int[numNeighbours - 1];

        int index = 0;

        for (int i = 0; i < nodes.length; i++)
            if (nodes[i] != -1) {
                if (includeReverse || (curDir < 0 || curDir > 3) || numNeighbours == 1)
                    directions[index++] = i;
                else if (i != getReverse(curDir))
                    try {
                        directions[index++] = i;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
            }

        return directions;
    }

    //Returns the direction Pac-Man should take to approach/retreat a target (to) given some distance
    //measure
    public int getNextPacManDir(int to, boolean closer, DM measure) {
        return getNextDir(maze.graph[curPacManLoc].neighbours, to, closer, measure);
    }

    //This method returns the direction to take given some options (usually corresponding to the
    //neighbours of the node in question), moving either towards or away (closer in {true, false})
    //using one of the three distance measures.
    private int getNextDir(int[] from, int to, boolean closer, Game.DM measure) {
        int dir = -1;

        double min = Integer.MAX_VALUE;
        double max = -Integer.MAX_VALUE;

        for (int i = 0; i < from.length; i++) {
            if (from[i] != -1) {
                double dist = 0;

                switch (measure) {
                    case PATH:
                        dist = getPathDistance(from[i], to);
                        break;
                    case EUCLID:
                        dist = getEuclideanDistance(from[i], to);
                        break;
                    case MANHATTEN:
                        dist = getManhattenDistance(from[i], to);
                        break;
                }

                if (closer && dist < min) {
                    min = dist;
                    dir = i;
                }

                if (!closer && dist > max) {
                    max = dist;
                    dir = i;
                }
            }
        }

        return dir;
    }

    //Returns the PATH distance from any node to any other node
//    public int getPathDistance(int from, int to) {
//        if (from == to)
//            return 0;
//        else if (from < to)
//            return maze.distances[((to * (to + 1)) / 2) + from];
//        else
//            return maze.distances[((from * (from + 1)) / 2) + to];
//    }

    public int getPathDistance(int from, int to) {
        if (from == to)
            return 0;
        return maze.distances.get(from).get(to);
    }

    //Returns the EUCLEDIAN distance between two nodes in the current maze.
    public double getEuclideanDistance(int from, int to) {
        return Math.sqrt(Math.pow(maze.graph[from].x - maze.graph[to].x, 2) + Math.pow(maze.graph[from].y - maze.graph[to].y, 2));
    }


    //Returns the MANHATTEN distance between two nodes in the current maze.
    public int getManhattenDistance(int from, int to) {
        return (int) (Math.abs(maze.graph[from].x - maze.graph[to].x) + Math.abs(maze.graph[from].y - maze.graph[to].y));
    }

    //Returns the path of adjacent nodes from one node to another, including these nodes
    //E.g., path from a to c might be [a,f,r,t,c]
    public int[] getPath(int from, int to) {
        if (from < 0 || to < 0) return new int[0];
        int currentNode = from;
        ArrayList<Integer> path = new ArrayList<>();
        int lastDir;

        while (currentNode != to) {
            path.add(currentNode);
            int[] neighbours = maze.graph[currentNode].neighbours;
            lastDir = getNextDir(neighbours, to, true, G.DM.PATH);
            currentNode = neighbours[lastDir];
        }

        int[] arrayPath = new int[path.size()];

        for (int i = 0; i < arrayPath.length; i++)
            arrayPath[i] = path.get(i);

        return arrayPath;
    }

    //Similar to getPath(-) but takes into consideration the fact that ghosts may not reverse. Hence the path to be taken
    //may be significantly longer than the shortest available path
    public int[] getGhostPath(String whichGhost, int to) {
        Ghost ghost = curGhosts.get(whichGhost);
        int currentNode = ghost.location;
        if (maze.graph[currentNode].numNeighbours == 0)
            return new int[0];

        ArrayList<Integer> path = new ArrayList<>();
        int lastDir = ghost.direction;

        while (currentNode != to) {
            path.add(currentNode);
            int[] neighbours = getGhostNeighbours(currentNode, lastDir);
            lastDir = getNextDir(neighbours, to, true, G.DM.PATH);
            currentNode = neighbours[lastDir];
        }

        int[] arrayPath = new int[path.size()];

        for (int i = 0; i < arrayPath.length; i++)
            arrayPath[i] = path.get(i);

        return arrayPath;
    }

    //Returns the node from 'targets' that is closest/farthest from the node 'from' given the distance measure specified
    public int getTarget(int from, int[] targets, boolean nearest, Game.DM measure) {
        int target = -1;

        double min = Integer.MAX_VALUE;
        double max = -Integer.MAX_VALUE;

        for (int i = 0; i < targets.length; i++) {
            double dist = 0;

            switch (measure) {
                case PATH:
                    dist = getPathDistance(targets[i], from);
                    break;
                case EUCLID:
                    dist = getEuclideanDistance(targets[i], from);
                    break;
                case MANHATTEN:
                    dist = getManhattenDistance(targets[i], from);
                    break;
            }

            if (nearest && dist < min) {
                min = dist;
                target = targets[i];
            }

            if (!nearest && dist > max) {
                max = dist;
                target = targets[i];
            }
        }

        return target;
    }

    //Returns the path distance for a particular ghost: takes into account the fact that ghosts may not reverse
    public int getGhostPathDistance(String whichGhost, int to) {
        return getGhostPath(whichGhost, to).length;
    }

    //Returns the neighbours of a node with the one correspodining to the reverse of direction being deleted (i.e., =-1)
    private int[] getGhostNeighbours(int node, int lastDirection) {
        int[] neighbours = Arrays.copyOf(maze.graph[node].neighbours, maze.graph[node].neighbours.length);
        neighbours[getReverse(lastDirection)] = -1;

        return neighbours;
    }

    /*
     * Stores the actual mazes, each of which is simply a connected graph. The differences between the mazes are the connectivity
     * and the x,y coordinates (used for drawing or to compute the Euclidean distance. There are 3 built-in distance functions in
     * total: Euclidean, Manhatten and Dijkstra's shortest path distance. The latter is pre-computed and loaded, the others are
     * computed on the fly whenever getNextDir(-) is called.
     */
    public static final class Maze {
        private String pathMazes = "resources/data";
        private String[] nodeNames = {"a", "b", "c", "d"};
        private String[] distNames = {"da", "db", "dc", "dd"};

        protected ArrayList<ArrayList<Integer>> distances;
        protected int[] pillIndices, powerPillIndices, junctionIndices;                //Information for the controllers
        public Node[] graph;                                                                //The actual maze, stored as a graph (set of nodes)
        protected int initialPacPosition, lairPosition, initialGhostsPosition, width, height;    //Maze-specific information
        protected String name;                                                                //Name of the Maze


        public Maze(int index) {
            loadNodes(nodeNames[index]);
            loadDistances(distNames[index]);
        }

        /*
         * Each maze is stored as a (connected) graph: all nodes have neighbours, stored in an array of length 4. The
         * index of the array associates the direction the neighbour is located at: '[up,right,down,left]'.
         * For instance, if node '9' has neighbours '[-1,12,-1,6]', you can reach node '12' by going right, and node
         * 6 by going left. The directions returned by the controllers should thus be in {0,1,2,3} and can be used
         * directly to determine the next node to go to.
         */
        public Maze(List<Tile> tiles, int width, int height) {
            int size = tiles.size();
            this.graph = new Node[size];
            for (int i = 0; i < size; i++) {
                var tile = tiles.get(i);
                var point = tile.getPoint();
                int[] neighbors = new int[4];
                var index = (int) (point.getX() + point.getY() * width);
                neighbors[0] = tile.getWalls(0) ? -1 : Math.max(index - width, -1);
                neighbors[1] = tile.getWalls(3) ? -1 : (index % (width) + 1) < (width) ? index + 1 : -1;
                neighbors[2] = tile.getWalls(2) ? -1 : (index + width) < size ? (index + width) : -1;
                neighbors[3] = tile.getWalls(1) ? -1 : (index % (width) - 1) > -1 ? index - 1 : -1;
                this.graph[index] = new Node(index, (int) point.getX(), (int) point.getY(), neighbors);
            }
            this.width = width;
            this.height = height;
//			loadNodes(nodeNames[index]);
//			loadDistances(distNames[index]);
			System.out.println("asdas");
        }

        //Loads all the nodes from files and initialises all maze-specific information.
        private void loadNodes(String fileName) {
            try {
                //APPLET
                //BufferedReader br=new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/data/"+fileName)));
                //APPLICATION

                // ORIG:
                //BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(pathMazes+System.getProperty("file.separator")+fileName)));
                BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("data/" + fileName)));
                String input = br.readLine();

                //preamble
                String[] pr = input.split("\t");
                this.name = pr[0];
                this.initialPacPosition = Integer.parseInt(pr[1]);
                this.lairPosition = Integer.parseInt(pr[2]);
                this.initialGhostsPosition = Integer.parseInt(pr[3]);
                this.graph = new Node[Integer.parseInt(pr[4])];
                this.pillIndices = new int[Integer.parseInt(pr[5])];
                this.powerPillIndices = new int[Integer.parseInt(pr[6])];
                this.junctionIndices = new int[Integer.parseInt(pr[7])];
                this.width = Integer.parseInt(pr[8]);
                this.height = Integer.parseInt(pr[9]);

                input = br.readLine();

                int nodeIndex = 0;
                int pillIndex = 0;
                int powerPillIndex = 0;
                int junctionIndex = 0;

                while (input != null) {
                    String[] nd = input.split("\t");
                    Node node = new Node(nd[0], nd[1], nd[2], nd[7], nd[8], new String[]{nd[3], nd[4], nd[5], nd[6]});

                    graph[nodeIndex++] = node;

                    if (node.pillIndex >= 0)
                        pillIndices[pillIndex++] = node.nodeIndex;
                    else if (node.powerPillIndex >= 0)
                        powerPillIndices[powerPillIndex++] = node.nodeIndex;

                    if (node.numNeighbours > 2)
                        junctionIndices[junctionIndex++] = node.nodeIndex;

                    input = br.readLine();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        /*
         * Loads the shortest path distances which have been pre-computed. The data contains the shortest distance from
         * any node in the maze to any other node. Since the graph is symmetric, the symmetries have been removed to preserve
         * memory and all distances are stored in a 1D array; they are looked-up using getDistance(-).
         */
        private void loadDistances(String fileName) {
//            this.distances = new int[((graph.length * (graph.length - 1)) / 2) + graph.length];

//            try {
//                //APPLET
////	        	BufferedReader br=new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/data/"+fileName)));
//                //APPLICATION
//
//                //ORIG:
//                //BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(pathMazes+System.getProperty("file.separator")+fileName)));
//                BufferedReader br = new BufferedReader(new InputStreamReader((ClassLoader.getSystemClassLoader().getResourceAsStream("data/" + fileName))));
//
//                String input = br.readLine();
//
//                int index = 0;
//
//                while (input != null) {
//                    distances[index++] = Integer.parseInt(input);
//                    input = br.readLine();
//                }
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//            }


        }
    }

    /*
     * Stores all information relating to a node in the graph, including all the indices required
     * to check and update the current state of the game.
     */
    public final static class Node {
        public final int nodeIndex, x, y;
        public final int numNeighbours;
        public volatile int pillIndex, powerPillIndex;
        protected final int[] neighbours;

        protected Node(int nodeIndex, int x, int y, int[] neighbours) {
            this.nodeIndex = nodeIndex;
            this.x = x;
            this.y = y;
            this.neighbours = neighbours;

            int numNeighbours = 0;
            for (int neighbour : neighbours) {
                if(neighbour != -1) {
                    numNeighbours++;
                }
            }
            this.numNeighbours = numNeighbours;
        }

        protected Node(String nodeIndex, String x, String y, String pillIndex, String powerPillIndex, String[] neighbours) {
            this.nodeIndex = Integer.parseInt(nodeIndex);
            this.x = Integer.parseInt(x);
            this.y = Integer.parseInt(y);
            this.pillIndex = Integer.parseInt(pillIndex);
            this.powerPillIndex = Integer.parseInt(powerPillIndex);

            this.neighbours = new int[neighbours.length];
            int numNeighbours = 0;
            for (int i = 0; i < neighbours.length; i++) {
                this.neighbours[i] = Integer.parseInt(neighbours[i]);

                if (this.neighbours[i] != -1)
                    numNeighbours++;
            }
            this.numNeighbours = numNeighbours;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public static class Ghost {
        public final String uuid;
        public volatile int location;
        public volatile int direction;

        public Ghost(String uuid, int location, int direction) {
            this.uuid = uuid;
            this.location = location;
            this.direction = direction;
        }
    }
}