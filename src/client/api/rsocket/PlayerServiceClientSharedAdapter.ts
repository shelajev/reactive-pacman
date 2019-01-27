import { Flux } from "reactor-core-js/flux";
import { Single } from "rsocket-flowable";
import { Player } from "@shared/player_pb";
import { Location } from "@shared/location_pb";
import PlayerService from "../PlayerService";
import { ReactiveSocket } from "rsocket-types";
import { PlayerServiceClient } from "@shared/service_rsocket_pb";
import { Point } from "@shared/point_pb";
import { Empty } from "google-protobuf/google/protobuf/empty_pb";

export default class PlayerServiceClientSharedAdapter implements PlayerService {
    private service: any;

    constructor(rSocket: ReactiveSocket<any, any>) {
        this.service = new PlayerServiceClient(rSocket);
    }

    locate(location: Location.AsObject): Single<void> {
        return new Single(subject => {
            let cancelled: boolean = false;
            let cancelCallback = () => {
                cancelled = true;
            }

            subject.onSubscribe(cancelCallback);

            try {
                if (!cancelled) {
                    const locationProto = new Location();
                    const positionProto = new Point();

                    positionProto.setX(location.position.x);
                    positionProto.setY(location.position.y);
                    locationProto.setPosition(positionProto);
                    locationProto.setDirec(location.direc);
                    locationProto.setFlipX(location.flipX);
                    locationProto.setRotation(location.rotation);

                    this.service.locate(locationProto);
                }

                if (!cancelled) {
                    subject.onComplete()
                }
            } catch (e) {
                if (!cancelled) {
                    subject.onError(e);
                }
            }
        })
    }

    players(): Flux<Player.AsObject> {
        return Flux.from<Player>(this.service.players(new Empty()))
            .map(player => player.toObject())
    }
}