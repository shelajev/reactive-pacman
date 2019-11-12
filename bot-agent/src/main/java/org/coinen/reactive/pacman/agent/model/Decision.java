package org.coinen.reactive.pacman.agent.model;

import org.coinen.reactive.pacman.agent.controllers.Direction;

public class Decision {
    public static final Decision UNCHANGED = new Decision(Direction.NONE);

    final Direction direction;

    public Decision(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "Decision{" +
                "direction=" + direction +
                '}';
    }
}
