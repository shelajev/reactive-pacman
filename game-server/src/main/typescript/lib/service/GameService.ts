import { Mono } from 'reactor-core-js/flux';
import { Config } from '@shared/config_pb';
import { Nickname } from '@shared/player_pb';

export default interface GameService {

    start(nickname: Nickname): Config;

}
