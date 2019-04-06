import * as rsocket_flowable from 'rsocket-flowable';
import * as google_protobuf_empty_pb from 'google-protobuf/google/protobuf/empty_pb';
import {Location, Player, RSocketRPCServices} from "game-idl";
import {PlayerService} from '../../service';
import {Disposable} from 'reactor-core-js';
import FlowableAdapter from './support/FlowableAdapter';
import {Flux} from 'reactor-core-js/flux';

export class PlayerController implements RSocketRPCServices.PlayerService {
    constructor(private playerService: PlayerService, private uuid: string) {}

    locate(messages: rsocket_flowable.Flowable<Location>, metadata?: Buffer): rsocket_flowable.Single<google_protobuf_empty_pb.Empty> {
        return new rsocket_flowable.Flowable(subject => {
            let disposable: Disposable = {
                dispose: () => {}
            };

            subject.onSubscribe({
                request: () => {},
                cancel: () => disposable.dispose()
            });

            disposable = Flux
                .from<Location>(FlowableAdapter.wrap(messages as any))
                .consume(
                    (location) => {
                        this.playerService.locate(this.uuid, location);
                    },
                    (e: Error) => subject.onError(e),
                    () => subject.onComplete()
                )
        }) as any;
    }

    players(message: google_protobuf_empty_pb.Empty, metadata?: Buffer): rsocket_flowable.Flowable<Player> {
        return this.playerService.players() as any;
    }

}
