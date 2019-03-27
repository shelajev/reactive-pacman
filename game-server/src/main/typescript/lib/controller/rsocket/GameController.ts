
import * as rsocket_flowable from 'rsocket-flowable';
import * as google_protobuf_empty_pb from 'google-protobuf/google/protobuf/empty_pb';
import { GameService as GameServicePB } from "@shared/service_rsocket_pb";
import FlowableAdapter from './support/FlowableAdapter';
import { ExtrasService, GameService } from '../../service';
import { Extra } from "@shared/extra_pb";
import { Nickname } from '@shared/player_pb';
import { Mono } from 'reactor-core-js/flux';
import { Config } from '@shared/config_pb';

export class GameController implements GameServicePB {
    constructor(private gameService: GameService) {}

    start(message: Nickname, metadata?: Buffer): rsocket_flowable.Single<Config> {
        return FlowableAdapter.wrap(this.gameService.start(message) as any) as any;
    }

}