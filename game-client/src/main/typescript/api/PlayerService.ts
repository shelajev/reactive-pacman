import { Flux } from "reactor-core-js/flux";
import { Single } from "rsocket-flowable";
import { Player } from "game-idl";
import { Location } from "game-idl";

export default interface PlayerService {

    locate(locationStream: Flux<Location.AsObject>): Single<void>
    
    players(): Flux<Player.AsObject>;
}