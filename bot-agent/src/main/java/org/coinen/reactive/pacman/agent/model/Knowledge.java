package org.coinen.reactive.pacman.agent.model;

public class Knowledge {
    public final String uuid;
    public final GameState gameState;
    public final CaseStudy caseStudy;
    public final int usage;

    public Knowledge(String uuid, GameState gameState, CaseStudy caseStudy, int usage) {
        this.uuid = uuid;
        this.gameState = gameState;
        this.caseStudy = caseStudy;
        this.usage = usage;
    }

    @Override
    public String toString() {
        return "Knowledge{" +
                "uuid='" + uuid + '\'' +
                ", gameState=" + gameState +
                ", caseStudy=" + caseStudy +
                ", usage=" + usage +
                '}';
    }
}
