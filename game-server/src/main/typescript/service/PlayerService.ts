import {Flux} from 'reactor-core-js/flux';
import {Location, Player} from 'game-idl';

export default interface PlayerService {

    createPlayer(uuid: string, nickname: string): Player;

    disconnectPlayer(uuid: string): void;

    locate(uuid: string, locationStream: Flux<Location>): Flux<Location>;

    players(): Flux<Player> ;

}
