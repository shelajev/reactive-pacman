package org.coinen.reactive.pacman.agent.model;

public class History {
    public final int ongoingGain;
    public final Outcome previousOutcome;
    public final Knowledge previousKnowledge;
    public final Outcome currentOutcome;
    public final Knowledge currentKnowledge;

    public final Knowledge toStore;

    public History(int ongoingGain, Outcome previousOutcome, Knowledge previousKnowledge, Outcome currentOutcome, Knowledge currentKnowledge, Knowledge toStore) {
        this.ongoingGain = ongoingGain;
        this.previousOutcome = previousOutcome;
        this.previousKnowledge = previousKnowledge;
        this.currentOutcome = currentOutcome;
        this.currentKnowledge = currentKnowledge;
        this.toStore = toStore;
    }
}
