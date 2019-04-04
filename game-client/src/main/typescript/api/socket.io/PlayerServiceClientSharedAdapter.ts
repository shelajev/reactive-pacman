import {DirectProcessor, Flux} from "reactor-core-js/flux";
import {Single} from "rsocket-flowable";
import {Location, Player, Point} from "game-idl";
import PlayerService from "../PlayerService";
import {Disposable} from "reactor-core-js";
import {IMeterRegistry} from "rsocket-rpc-metrics";

export default class PlayerServiceClientSharedAdapter implements PlayerService {

    private readonly sharedPlayersStream: DirectProcessor<Player.AsObject>;

    constructor(private readonly socket: SocketIOClient.Socket, meterRegistry: IMeterRegistry) {
        // this.service = new RSocketRPCServices.PlayerServiceClient(rSocket, undefined, meterRegistry);
        this.sharedPlayersStream = new DirectProcessor<Player.AsObject>();
        socket.on("players", (data: ArrayBuffer) => {
            if (data && data.byteLength) {
                this.sharedPlayersStream.onNext(Player.deserializeBinary(new Uint8Array(data)).toObject());
            }
        })
    }

    locate(locationStream: Flux<Location.AsObject>): Single<void> {
        return new Single(subject => {
            let isDisposed = false;
            let disposable: Disposable = {
                dispose: () => {
                    isDisposed = true;
                }
            };
            
            subject.onSubscribe(() => disposable.dispose());

            if (!isDisposed) {
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
                    .consume(
                        (location: Location) => ((this.socket as any).binary(true) as SocketIOClient.Socket).emit("locate", location.serializeBinary()),
                        (e: Error) => subject.onError(e),
                        () => subject.onComplete()
                    )
            }
        })
    }

    players(): Flux<Player.AsObject> {
        // if (!this.sharedPlayersStream) {
        //     this.sharedPlayersStream = new DirectProcessor();
        //     Flux.from<Player>(FlowableAdapter.wrap(this.service.players(new Empty()) as any))
        //         .map(player => player.toObject())
        //         .subscribe(this.sharedPlayersStream);
        // }
        
        return this.sharedPlayersStream;
    }
}