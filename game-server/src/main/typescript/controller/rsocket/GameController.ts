
import * as rsocket_flowable from 'rsocket-flowable';
import { GameService as GameServicePB } from "@shared/service_rsocket_pb";
import { GameService } from '../../service';
import { Nickname } from '@shared/player_pb';
import { Config } from '@shared/config_pb';

export class GameController implements GameServicePB {
    constructor(private readonly uuid: string, private gameService: GameService) {}

    start(message: Nickname, metadata?: Buffer): rsocket_flowable.Single<Config> {
        return rsocket_flowable.Single.of(this.gameService.start(this.uuid, message));
    }

}