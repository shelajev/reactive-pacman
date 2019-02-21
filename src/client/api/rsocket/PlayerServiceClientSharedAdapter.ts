import { Flux } from "reactor-core-js/flux";
import { Single } from "rsocket-flowable";
import { Player } from "@shared/player_pb";
import { Location } from "@shared/location_pb";
import PlayerService from "../PlayerService";
import { ReactiveSocket } from "rsocket-types";
import { PlayerServiceClient } from "@shared/service_rsocket_pb";
import { Point } from "@shared/point_pb";
import { Empty } from "google-protobuf/google/protobuf/empty_pb";
import { Disposable } from "reactor-core-js";

export default class PlayerServiceClientSharedAdapter implements PlayerService {
    private service: any;

    constructor(rSocket: ReactiveSocket<any, any>) {
        this.service = new PlayerServiceClient(rSocket);
    }

    locate(locationStream: Flux<Location.AsObject>): Single<void> {
        return new Single(subject => {
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
                .compose(flux => this.service.locate(flux))
                .consume(
                    () => {},
                    (e: Error) => subject.onError(e),
                    () => subject.onComplete()
                )
        })
    }

    players(): Flux<Player.AsObject> {
        return Flux.from<Player>(this.service.players(new Empty()))
            .map(player => player.toObject())
    }
}