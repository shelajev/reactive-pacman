
import * as rsocket_flowable from 'rsocket-flowable';
import * as google_protobuf_empty_pb from 'google-protobuf/google/protobuf/empty_pb';
import { PlayerService as PlayerServicePB } from "@shared/service_rsocket_pb";
import FlowableAdapter from './support/FlowableAdapter';
import { ExtrasService, GameService, PlayerService } from '../../service';
import { Location } from "@shared/location_pb";
import { Nickname, Player } from '@shared/player_pb';
import { Mono } from 'reactor-core-js/flux';
import { Config } from '@shared/config_pb';
import { Publisher } from 'reactor-core-js/reactive-streams-spec';
import { Disposable } from 'reactor-core-js';

export class PlayerController implements PlayerServicePB {
    constructor(private playerService: PlayerService, private uuid: string) {}

    locate(message: rsocket_flowable.Flowable<Location>, metadata?: Buffer): rsocket_flowable.Single<google_protobuf_empty_pb.Empty> {
        return new rsocket_flowable.Single(subject => {
            let disposable: Disposable = {
                dispose: () => {}
            };
            
            subject.onSubscribe(() => disposable.dispose());

            disposable = this.playerService.locate(this.uuid, message as any)
                .consume(
                    () => {},
                    (e: Error) => subject.onError(e),
                    () => subject.onComplete(new google_protobuf_empty_pb.Empty())
                )
        });
    }

    players(message: google_protobuf_empty_pb.Empty, metadata?: Buffer): rsocket_flowable.Flowable<Player> {
        return this.playerService.players() as any;
    }

}
