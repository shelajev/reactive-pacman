import { Mono } from 'reactor-core-js/flux';
import { Config } from '@shared/config_pb';
import { Nickname } from '@shared/player_pb';

export default interface GameService {

    start(uuid: string, nickname: Nickname): Config;

}
