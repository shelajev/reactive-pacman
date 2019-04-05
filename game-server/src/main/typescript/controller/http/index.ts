import { Express } from 'express';
import ExtrasController from './HttpExtrasController';
import GameController from './HttpGameController';
import PlayerController from './HttpPlayerController';
import SetupController from './HttpSetupController';
import { ExtrasService, GameService, PlayerService, MapService } from '../../service';

export default function controllers(
  app: Express,
  extrasService: ExtrasService,
  gameService: GameService,
  playerService: PlayerService,
  mapService: MapService,
): void {
  const extrasController = ExtrasController(app, extrasService);
  const gameController = GameController(app, gameService);
  const playerController = PlayerController(app, playerService);
  const setupController = SetupController(app, mapService);
}
