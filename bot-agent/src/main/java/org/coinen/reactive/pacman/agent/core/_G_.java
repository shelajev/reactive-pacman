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

import org.coinen.reactive.pacman.agent.PacManSimulator.GameConfig;
import org.coinen.reactive.pacman.agent.controllers.Direction;
import org.coinen.pacman.*;
import qlearn.Q_learn;

import java.util.ArrayList;
import java.util.BitSet;

public class _G_ extends G {
    public static final int EDIBLE_ALERT = 30;    //for display only (ghosts turning blue)

    //to save replays
    private int pacManDir = INITIAL_PAC_DIR;
    private StringBuilder sb;

    public _G_() {
    }

    //Instantiates everything to start a new game
    public void newGame(Map map, Config serverConfig) {
        Size size = map.getSize();
        maze = new G.Maze(map.getTilesList(), size.getWidth(), size.getHeight());
//		init();		//load mazes if not yet loaded

        Player player = serverConfig.getPlayer();

        Point position = player.getLocation().getPosition();
        maze.initialPacPosition = (int) (position.getX() / 100) + (int) (position.getY() / 100) * size.getWidth();
        ArrayList<Integer> pill = new ArrayList<>();
        ArrayList<Integer> power = new ArrayList<>();
        int pillCount = 0;
        int powerCount = 0;
        for (var extra : serverConfig.getExtrasList()) {
            if (Math.signum(extra) == -1) {
                int absExtra = Math.abs(extra);
                power.add(absExtra);
                maze.graph[absExtra].powerPillIndex = powerCount++;
                maze.graph[absExtra].pillIndex = -1;
            } else {
                pill.add(extra);
                maze.graph[extra].powerPillIndex = -1;
                maze.graph[extra].pillIndex = pillCount++;
            }
        }
        maze.pillIndices = pill.stream().mapToInt(Integer::intValue).toArray();
        maze.powerPillIndices = power.stream().mapToInt(Integer::intValue).toArray();
        maze.distances = new ArrayList<>();

        int l = Q_learn.currDistancesUp.size();
        for (int i = 0; i < l; i++) {
            ArrayList<Integer> al = new ArrayList<>();
            maze.distances.add(al);
            ArrayList<Integer> cdu = Q_learn.currDistancesUp.get(i);
            ArrayList<Integer> cdr = Q_learn.currDistancesRight.get(i);
            ArrayList<Integer> cdl = Q_learn.currDistancesLeft.get(i);
            ArrayList<Integer> cdd = Q_learn.currDistancesDown.get(i);
            for (int j = 0; j < cdu.size(); j++) {
                al.add(
                    j,
                    Math.min(
                        cdl.get(j),
                        Math.min(
                            cdd.get(j),
                            Math.min(cdu.get(j), cdr.get(j))
                        )
                    )
                );
            }
        }

        curMaze = 0;

        pills = new BitSet(getNumberPills());
        pills.set(0, getNumberPills());
        powerPills = new BitSet(getNumberPowerPills());
        powerPills.set(0, getNumberPowerPills());
        score = 0;
        levelTime = 0;
        totalTime = 0;
        totLevel = 0;
        livesRemaining = NUM_LIVES;
        extraLife = false;
        gameOver = false;

        reset(false);

        for (Player p : serverConfig.getPlayersList()) {
            if (p.getType() == Player.Type.GHOST) {
                Location location = p.getLocation();
                Point pos = location.getPosition();
                org.coinen.reactive.pacman.agent.controllers.Direction direction = Direction.valueOf(location.getDirection().name());
                int locationIndex = (int) (pos.getX() / 100) + (int) (pos.getY() * getWidth() / 100);
                curGhosts.put(p.getUuid(), new Ghost(p.getUuid(), locationIndex, direction.index));
            }
        }

        curPacManLoc = maze.initialPacPosition;
        lastPacManDir = getPossiblePacManDirs()[0];
        //for replays
        this.sb = new StringBuilder();
    }

    //Size of the Maze (for display only)
    public int getWidth() {
        return maze.width;
    }

    //Size of the Maze (for display only)
    public int getHeight() {
        return maze.height;
    }

    //for the web-site javascript replays
    public void monitorGame() {
        sb.append("{");

        //maze
        sb.append("ma:" + curMaze + ",");
        sb.append("tt:" + totalTime + ",");
        sb.append("li:" + livesRemaining + ",");
        sb.append("sc:" + score + ",");
        sb.append("lt:" + levelTime + ",");
        sb.append("le:" + totLevel + ",");

        // pacman
        sb.append("pn:" + curPacManLoc + ",");

        int pacDir = lastPacManDir;

        if (pacDir >= 0 && pacDir < 4)
            pacManDir = pacDir;

        sb.append("pd:" + pacManDir + ",");

//        // ghosts
//        sb.append("gh:[");
//        sb.append("{gn:" + curGhostLocs[0] + ",");
//        sb.append("di:" + lastGhostDirs[0] + ",et:" + edibleTimes[0]);
//        sb.append(",lt:" + lairTimes[0]);
//        sb.append("},");
//        sb.append("{gn:" + curGhostLocs[1] + ",");
//        sb.append("di:" + lastGhostDirs[1] + ",et:" + edibleTimes[1]);
//        sb.append(",lt:" + lairTimes[1]);
//        sb.append("},");
//        sb.append("{gn:" + curGhostLocs[2] + ",");
//        sb.append("di:" + lastGhostDirs[2] + ",et:" + edibleTimes[2]);
//        sb.append(",lt:" + lairTimes[2]);
//        sb.append("},");
//        sb.append("{gn:" + curGhostLocs[3] + ",");
//        sb.append("di:" + lastGhostDirs[3] + ",et:" + edibleTimes[3]);
//        sb.append(",lt:" + lairTimes[3]);
//        sb.append("}");
//        sb.append("],");

        // pills
        sb.append("pi:\"");

        for (int i = 0; i < getPillIndices().length; i++)
            if (checkPill(i))
                sb.append("1");
            else
                sb.append("0");

        sb.append("\",");
        sb.append("po:\"");

        for (int i = 0; i < getPowerPillIndices().length; i++)
            if (checkPowerPill(i))
                sb.append("1");
            else
                sb.append("0");

        sb.append("\"");
        sb.append("},\n");
    }

    public StringBuilder getRecordedMatch() {
        return sb;
    }
}