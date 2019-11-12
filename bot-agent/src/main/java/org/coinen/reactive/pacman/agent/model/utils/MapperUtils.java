package org.coinen.reactive.pacman.agent.model.utils;

import org.coinen.pacman.learning.CaseStudy;
import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.model.Knowledge;

public class MapperUtils {

    public static org.coinen.pacman.learning.Knowledge mapToProto(Knowledge k) {
        return org.coinen.pacman.learning.Knowledge.newBuilder()
                .setUuid(k.uuid)
                .setCaseStudy(
                        CaseStudy.newBuilder()
                                .setWeightUp(k.caseStudy.upWeight)
                                .setWeightDown(k.caseStudy.downWeight)
                                .setWeightRight(k.caseStudy.rightWeight)
                                .setWeightLeft(k.caseStudy.leftWeight)
                )
                .setGameState(
                    org.coinen.pacman.learning.GameState.newBuilder()
                        .setPillUp(k.gameState.pillUp)
                        .setPillDown(k.gameState.pillDown)
                        .setPillRight(k.gameState.pillRight)
                        .setPillLeft(k.gameState.pillLeft)
                        .setPowerPillUp(k.gameState.powerPillUp)
                        .setPowerPillDown(k.gameState.powerPillDown)
                        .setPowerPillRight(k.gameState.powerPillRight)
                        .setPowerPillLeft(k.gameState.powerPillLeft)
                        .setGhostUp(k.gameState.ghostUp)
                        .setGhostDown(k.gameState.ghostDown)
                        .setGhostRight(k.gameState.ghostRight)
                        .setGhostLeft(k.gameState.ghostLeft)
                        .setEdibleGhostUp(k.gameState.edibleGhostUp)
                        .setEdibleGhostDown(k.gameState.edibleGhostDown)
                        .setEdibleGhostRight(k.gameState.edibleGhostRight)
                        .setEdibleGhostLeft(k.gameState.edibleGhostLeft)
                        .setIntersectionUp(k.gameState.intersectionUp)
                        .setIntersectionDown(k.gameState.intersectionDown)
                        .setIntersectionRight(k.gameState.intersectionRight)
                        .setIntersectionLeft(k.gameState.intersectionLeft)
                )
                .build();
    }

    public static Knowledge mapToModel(org.coinen.pacman.learning.Knowledge k) {
        return new Knowledge(
            k.getUuid(),
            new GameState(
                k.getGameState().getPillUp(),
                k.getGameState().getPillRight(),
                k.getGameState().getPillDown(),
                k.getGameState().getPillLeft(),
                k.getGameState().getGhostUp(),
                k.getGameState().getGhostRight(),
                k.getGameState().getGhostDown(),
                k.getGameState().getGhostLeft(),
                k.getGameState().getPowerPillUp(),
                k.getGameState().getPowerPillRight(),
                k.getGameState().getPowerPillDown(),
                k.getGameState().getPowerPillLeft(),
                k.getGameState().getEdibleGhostUp(),
                k.getGameState().getEdibleGhostRight(),
                k.getGameState().getEdibleGhostDown(),
                k.getGameState().getEdibleGhostLeft(),
                k.getGameState().getIntersectionUp(),
                k.getGameState().getIntersectionRight(),
                k.getGameState().getIntersectionDown(),
                k.getGameState().getIntersectionLeft()
            ),
            new org.coinen.reactive.pacman.agent.model.CaseStudy(
                k.getCaseStudy().getWeightUp(),
                k.getCaseStudy().getWeightRight(),
                k.getCaseStudy().getWeightDown(),
                k.getCaseStudy().getWeightLeft()
            ),
            0
        );
    }
}
