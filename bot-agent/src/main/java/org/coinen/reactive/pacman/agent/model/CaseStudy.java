package org.coinen.reactive.pacman.agent.model;

public class CaseStudy {
    public final float upWeight;
    public final float rightWeight;
    public final float downWeight;
    public final float leftWeight;

    public CaseStudy(float upWeight, float rightWeight, float downWeight, float leftWeight) {
        this.upWeight = upWeight;
        this.rightWeight = rightWeight;
        this.downWeight = downWeight;
        this.leftWeight = leftWeight;
    }

    @Override
    public String toString() {
        return "CaseStudy{" +
                "upWeight=" + upWeight +
                ", rightWeight=" + rightWeight +
                ", downWeight=" + downWeight +
                ", leftWeight=" + leftWeight +
                '}';
    }
}
