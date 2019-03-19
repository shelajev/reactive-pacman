const { DirectProcessor } = require('reactor-core-js/flux');

import { Single } from 'rsocket-flowable';

import { Nickname } from '@shared/player_pb';
import { Config } from '@shared/config_pb';
import { Player } from '@shared/player_pb';
import { Location, Direction } from '@shared/location_pb';
import { Point } from '@shared/point_pb';
import getNewPlayerType from '../generatePlayerType';
import store from '../../store';
import { playersProcessor } from '../processors';
import findBestStartingPosition from '../bestPositionFinder';

const gameService = {
    start(nickname: Nickname, uuid: string) {
        if (nickname.getValue().length <= 13 && !store.getPlayer(uuid)) {
            let name = nickname.getValue().replace(/[^a-zA-Z0-9. ]/g, '');
            if (name == "") {
                name = "Unnamed";
            }
            const score = 10;
            const playerType = getNewPlayerType();
            const pos = findBestStartingPosition(playerType);
            const config = new Config();
            const player = new Player();
            const playerPosition = new Point();
            const location = new Location();

            
            playerPosition.setX(pos.x);
            playerPosition.setY(pos.y);

            location.setDirection(Direction.RIGHT);
            location.setPosition(playerPosition);

            player.setLocation(location);
            player.setNickname(name);
            player.setState(Player.State.CONNECTED);
            player.setScore(score);
            player.setType(playerType);
            player.setUuid(uuid);

            
            config.setPlayersList(Object.keys(store.getPlayers()).map(k => store.getPlayer(k)))
            config.setPlayer(player);
            config.setExtrasList(store.getMaze().extras.toArray());
            config.setScoresList(store.getLeaderBoard());
            playersProcessor.onNext(player);
            
            store.setPlayer(player);
            
            return Single.of(config);
        }

        return Single.error(new Error("wrong name"));
    }
};

export default gameService;
