import {GameService, PlayerService} from '../index';
import {Config, Nickname, Player, Score} from 'game-idl';
import {ExtrasRepository, PlayerRepository} from '../../repository';

export default class DefaultGameService implements GameService {
  constructor(
    private playerService: PlayerService,
    private extrasRepository: ExtrasRepository,
    private playerRepository: PlayerRepository,
  ) {

  }

  start(uuid: string, nickname: Nickname): Config {
    if (nickname.getValue().length <= 13) {
      let name = nickname.getValue().replace(/[^a-zA-Z0-9. ]/g, '');
      if (name.length === 0) {
        name = 'Boss of this gym';
      }
      const player = this.playerService.createPlayer(uuid, name);
      const config = new Config();
      config.setPlayer(player);
      config.setPlayersList(this.playerRepository.findAll()
        .filter((p: Player) => p.getUuid() !== player.getUuid())
      );
      config.setExtrasList(Array.from(this.extrasRepository.findAll()));
      config.setScoresList(this.playerRepository.findAll()
          .map((p: Player) => {
            const score = new Score();
            score.setUsername(p.getNickname());
            score.setScore(p.getScore());
            score.setUuid(p.getUuid());
            return score;
          }));
      console.log(config.toObject().extrasList);
      return config;
    } else {
      throw new Error('Invalid nickname');
    }
  }
}
