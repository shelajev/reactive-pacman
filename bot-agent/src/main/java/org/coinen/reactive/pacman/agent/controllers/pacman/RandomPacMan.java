package org.coinen.reactive.pacman.agent.controllers.pacman;

import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core.Game;
import org.coinen.reactive.pacman.agent.model.GameState;

public final class RandomPacMan extends PacManHijackController
{
	GameState lastState = null;  // Antonio: estado nuevo?
	
	
	
	
	@Override
	public void tick(Game game, long timeDue) {
		int[] directions=game.getPossiblePacManDirs();		//set flag as true to include reversals
		
		
		
		if(game.isJunction(game.getCurPacManLoc()) || game.isCorner(game.getCurPacManLoc()))
			pacman.set(directions[G.rnd.nextInt(directions.length)]);
	}
}