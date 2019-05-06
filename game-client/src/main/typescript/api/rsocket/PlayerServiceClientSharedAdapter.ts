import { Flux, DirectProcessor } from "reactor-core-js/flux";
import { Single } from "rsocket-flowable";
import { Player } from "game-idl";
import { Location } from "game-idl";
import PlayerService from "../PlayerService";
import { ReactiveSocket } from "rsocket-types";
import { RSocketRPCServices } from "game-idl";
import { Point } from "game-idl";
import { Empty } from "google-protobuf/google/protobuf/empty_pb";
import { Disposable } from "reactor-core-js";
import FlowableAdapter from "../FlowableAdapter";
import {IMeterRegistry} from "rsocket-rpc-metrics";

export default class PlayerServiceClientSharedAdapter implements PlayerService {

    private service: RSocketRPCServices.PlayerService;
    private sharedPlayersStream: DirectProcessor<Player.AsObject>;

    constructor(rSocket: ReactiveSocket<any, any>, meterRegistry?: IMeterRegistry) {
        this.service = new RSocketRPCServices.PlayerServiceClient(rSocket, undefined, meterRegistry);
    }

    locate(locationStream: Flux<Location.AsObject>): Single<void> {
        return new Single(subject => {
            const uuid = localStorage.getItem("uuid");
            const metadata = uuid ? Buffer.alloc(Buffer.byteLength(uuid), uuid, "utf8") : undefined;
            let disposable: Disposable = {
                dispose: () => {}
            };
            
            subject.onSubscribe(() => disposable.dispose());

            disposable = locationStream
                .map(location => {
                    const locationProto = new Location();
                    const positionProto = new Point();

                    positionProto.setX(location.position.x);
                    positionProto.setY(location.position.y);
                    locationProto.setPosition(positionProto);
                    locationProto.setDirection(location.direction);

                    return locationProto;
                })
                .compose(flux => FlowableAdapter.wrap(this.service.locate(flux as any, metadata) as any))
                .consume(
                    () => {},
                    (e: Error) => subject.onError(e),
                    () => subject.onComplete()
                )
        })
    }

    players(): Flux<Player.AsObject> {
        if (!this.sharedPlayersStream) {
            const uuid = localStorage.getItem("uuid");
            const metadata = uuid ? Buffer.alloc(Buffer.byteLength(uuid), uuid, "utf8") : undefined;
            this.sharedPlayersStream = new DirectProcessor();
            Flux.from<Player>(FlowableAdapter.wrap(this.service.players(new Empty(), metadata) as any))
                .map(player => player.toObject())
                .subscribe(this.sharedPlayersStream);
        }
        
        return this.sharedPlayersStream;
    }
}