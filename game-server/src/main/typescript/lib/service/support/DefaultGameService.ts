import { v4 as uuid } from 'uuid';
import { MapService } from '@shared/service_rsocket_pb';
import { PlayerService } from '../PlayerService';
import { GameService } from '../GameService';
import { Nickname, Player } from '@shared/player_pb';
import { Config } from '@shared/config_pb';
import { Score } from '@shared/score_pb';
import { PlayerRepository } from '../../repository/PlayerRepository';
import { ExtrasRepository } from '../../repository/ExtrasRepository';

export class DefaultGameService implements GameService {
  constructor(
    private playerService: PlayerService,
    private extrasRepository: ExtrasRepository,
    private playerRepository: PlayerRepository,
    private mapService: MapService
  ) {

  }

  start(nickname: Nickname): Config {
    if (nickname.getValue().length <= 13) {
      let name = nickname.getValue().replace(/[^a-zA-Z0-9. ]/g, '');
      if (name.length === 0) {
        name = 'Boss of this gym';
      }
      const id: string = uuid();
      const player = this.playerService.createPlayer(id, name);
      const config = new Config();
      config.setPlayer(player);
      this.playerRepository.findAll()
        .filter((p: Player) => !p.getUuid())
        .forEach((player, i) => config.addPlayers(player, i));
      this.extrasRepository.findAll()
        .forEach((extra, i) => config.addExtras(extra, i));
        this.playerRepository.findAll()
          .map((p: Player) => {
            const score = new Score();
            score.setUsername(p.getNickname());
            score.setScore(p.getScore());
            score.setUuid(p.getUuid());
            return score;
          })
          .forEach((score, i) => config.addScores(score, i));
      return config;
    } else {
      throw new Error('Invalid nickname');
    }
  }
}
