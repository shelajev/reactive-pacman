import { Flux } from "reactor-core-js/flux";
import { Single } from "rsocket-flowable";
import { Player } from "@shared/player_pb";
import { Location } from "@shared/location_pb";

export default interface PlayerService {

    locate(locationStream: Flux<Location.AsObject>): Single<void>
    
    players(): Flux<Player.AsObject>;
}