package org.coinen.reactive.pacman.agent.model;

import org.coinen.reactive.pacman.agent.controllers.Direction;

public class Decision {
    public static Decision UNCHENGED;

    final Direction direction;

    public Decision(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
