import { PlayerServiceServer } from '@shared/service_rsocket_pb';

import { Player } from '@shared/player_pb';
import { Location } from '@shared/location_pb';
import { Extra } from '@shared/extra_pb';
import { playersProcessor, extrasProcessor } from '../processors';
import store from '../../store';
// import scoreProcessor from '../processors/scoreProcessor';

const playerService = new PlayerServiceServer({
    locate(location: Location, uuid: string) {
        const time = Date.now();
        const player = store.getPlayer(uuid);

        player.setTimestamp(time);
        player.setState(Player.State.ACTIVE);
        player.setLocation(location);

        const collisionData = store.getMaze().collideFood(location.getPosition().getX(), location.getPosition().getY());
        if (collisionData) {
            if (Math.sign(collisionData) === 1)  {
                player.setScore(player.getScore() + 1);
                // scoreProcessor.onNext({player, score: player.getScore() + 1});
            } else {
                let sec = 10;
                store.setPowerUpEnd(Date.now() + sec * 1000);
            }
            let addedFood = store.getMaze().addFood(Object.keys(store.getPlayers()).length);
            
            const extra = new Extra();

            extra.setLast(collisionData);
            extra.setCurrent(addedFood);

            extrasProcessor.onNext(extra);
        }
        playersProcessor.onNext(player);
    },

    players() {
        return playersProcessor;
    }

})

export default playerService;
