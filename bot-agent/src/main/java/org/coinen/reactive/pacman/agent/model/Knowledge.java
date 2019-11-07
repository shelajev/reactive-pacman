package org.coinen.reactive.pacman.agent.model;

public class Knowledge {
    public final GameState gameState;
    public final CaseStudy caseStudy;

    public Knowledge(GameState gameState, CaseStudy caseStudy) {
        this.gameState = gameState;
        this.caseStudy = caseStudy;
    }
}
