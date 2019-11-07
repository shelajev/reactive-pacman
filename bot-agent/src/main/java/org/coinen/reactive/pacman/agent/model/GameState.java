package org.coinen.reactive.pacman.agent.model;

/**
 * This class represents a game state/case for PacMan game. States are used by learning algorithms in order to
 * study what action the agent should take depending on which state it is into.
 */
public final class GameState {

    public int index;
    public final int pillUp, pillDown, pillRight, pillLeft;                                    // Distance to pills in every direction
    public final int ghostUp, ghostDown, ghostRight, ghostLeft;                                // Distance to ghosts in every direction
    public final int edibleGhostUp, edibleGhostRight, edibleGhostDown, edibleGhostLeft;        // Distance to edible ghosts in every direction
    public final int powerPillUp, powerPillRight, powerPillDown, powerPillLeft;                // Distance to power pills in every direction
    public final int intersectionUp, intersectionRight, intersectionDown, intersectionLeft;    // Distance to intersections in every direction


    /* Game state/case representation. This info is both used in RL and CBR/RL systems.*/
    public GameState(int i, int pu, int pr, int pd, int pl, int gu, int gr, int gd, int gl,
                     int ppu, int ppr, int ppd, int ppl, int egu, int egr, int egd, int egl,
                     int iu, int ir, int id, int il) {
        index = i;
        pillUp = pu;
        pillRight = pr;
        pillDown = pd;
        pillLeft = pl;
        powerPillUp = ppu;
        powerPillRight = ppr;
        powerPillDown = ppd;
        powerPillLeft = ppl;
        ghostUp = gu;
        ghostRight = gr;
        ghostDown = gd;
        ghostLeft = gl;
        edibleGhostUp = egu;
        edibleGhostRight = egr;
        edibleGhostDown = egd;
        edibleGhostLeft = egl;
        intersectionUp = iu;
        intersectionRight = ir;
        intersectionDown = id;
        intersectionLeft = il;
    }

    /*Returns readable string with state data*/
    public String toString() {

        return " Index: " + index + " - Pills: " + pillUp + ", " + pillRight + ", " + pillDown + ", " + pillLeft +
                " - Ghosts: " + ghostUp + ", " + ghostRight + ", " + ghostDown + ", " + ghostLeft +
                " - Power Pills: " + powerPillUp + ", " + powerPillRight + ", " + powerPillDown + ", " + powerPillLeft +
                " - Edible Ghosts: " + edibleGhostUp + ", " + edibleGhostRight + ", " + edibleGhostDown + ", " + edibleGhostLeft +
                " - Intersections: " + intersectionUp + ", " + intersectionRight + ", " + intersectionDown + ", " + intersectionLeft;
    }

    /* Returns string with state data. Used for data storing*/
    public String data() {

        return index + " " + pillUp + " " + pillRight + " " + pillDown + " " + pillLeft +
                " " + ghostUp + " " + ghostRight + " " + ghostDown + " " + ghostLeft +
                " " + powerPillUp + " " + powerPillRight + " " + powerPillDown + " " + powerPillLeft +
                " " + edibleGhostUp + " " + edibleGhostRight + " " + edibleGhostDown + " " + edibleGhostLeft +
                " " + intersectionUp + " " + intersectionRight + " " + intersectionDown + " " + intersectionLeft;
    }

}
