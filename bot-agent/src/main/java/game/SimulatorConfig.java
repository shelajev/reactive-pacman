package game;

import game.PacManSimulator.GameConfig;
import game.controllers.pacman.IPacManController;

public class SimulatorConfig {

	public GameConfig game = new GameConfig();
	
	public IPacManController pacManController;

	/**
	 * How long can PacMan / Ghost controller think about the game before we compute next frame.
	 * If {@ #visualize} than it also determines the speed of the game.
	 * 
	 * DEFAULT: 25 FPS
	 */
	public int thinkTimeMillis = 40;
	
	
	
	public SimulatorConfig clone() {
		SimulatorConfig result = new SimulatorConfig();
		
		result.game = game.clone();
		
		result.pacManController = pacManController;

		result.thinkTimeMillis = thinkTimeMillis;
				
		return result;
	}

	public String getCSVHeader() {
		return game.getCSVHeader() + ";thinkTimeMillis;visualize;visualizeScale2x;mayBePaused;replay;replayFile";
	}
	

	public String getOptions() {
		return   SimulatorConfigOption.GAME_LEVELS_TO_PLAY.option + " " + game.levelsToPlay 
			   + " " + SimulatorConfigOption.GAME_POWER_PILLS.option + " " + game.powerPillsEnabled
			   + " " + SimulatorConfigOption.GAME_SEED.option + " " + game.seed
			   + " " + SimulatorConfigOption.GAME_TOTAL_PILLS.option + " " + game.totalPills
			   + " " + SimulatorConfigOption.SIM_THINK_TIME_MILLIS.option + " " + thinkTimeMillis;
	}
	
	public static SimulatorConfig fromOptions(String options) {
		String[] parts = options.split(" ");
		
		SimulatorConfig result = new SimulatorConfig();
		
		for (int i = 0; i < parts.length; i += 2) {
			String option = parts[i];
			String value = (i+1 < parts.length ? parts[i+1] : null);
			
			if (option.equals(SimulatorConfigOption.GAME_LEVELS_TO_PLAY.option)) {
				result.game.levelsToPlay = Integer.parseInt(value);				
			}
			if (option.equals(SimulatorConfigOption.GAME_POWER_PILLS.option)) {
				result.game.powerPillsEnabled = Boolean.parseBoolean(value);
			}
			if (option.equals(SimulatorConfigOption.GAME_SEED.option)) {
				result.game.seed = Integer.parseInt(value);
			}
			if (option.equals(SimulatorConfigOption.GAME_TOTAL_PILLS.option)) {
				result.game.totalPills = Double.parseDouble(value);
			}
			if (option.equals(SimulatorConfigOption.SIM_THINK_TIME_MILLIS.option)) {
				result.thinkTimeMillis = Integer.parseInt(value);
			}
		}
		
		return result;
	}
	
}
