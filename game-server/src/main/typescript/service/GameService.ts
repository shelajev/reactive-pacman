import {Config, Nickname} from 'game-idl';

export default interface GameService {

    start(uuid: string, nickname: Nickname): Config;

}
