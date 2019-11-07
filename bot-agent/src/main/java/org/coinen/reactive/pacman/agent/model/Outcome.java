package org.coinen.reactive.pacman.agent.model;

import org.coinen.pacman.Player;

public final class Outcome {
    public final GameState gameState;
    public final Decision decision;
    public final int gain;
    public final Type outcomeType;

    public Outcome(GameState gameState, int gain, Decision decision, Type outcomeType) {
        this.gameState = gameState;
        this.gain = gain;
        this.decision = decision;
        this.outcomeType = outcomeType;
    }

    public enum Type {
        NONE, KILLED, PILL_EATEN, POWER_EATEN, GHOST_EATEN;
    }
}
