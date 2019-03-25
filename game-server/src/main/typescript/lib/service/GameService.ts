import { Mono } from 'reactor-core-js/flux';
import { Config } from '@shared/config_pb';
import { Nickname } from '@shared/player_pb';

export interface GameService {

    start(nickname: Nickname): Config;

}
