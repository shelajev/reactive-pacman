import * as rsocket_flowable from 'rsocket-flowable';
import {Config, Nickname, RSocketRPCServices} from "game-idl";
import {GameService} from '../../service';

export class GameController implements RSocketRPCServices.GameService {
    constructor(private readonly uuid: string, private gameService: GameService) {}

    start(message: Nickname, metadata?: Buffer): rsocket_flowable.Single<Config> {
        return rsocket_flowable.Single.of(this.gameService.start(this.uuid, message));
    }

}