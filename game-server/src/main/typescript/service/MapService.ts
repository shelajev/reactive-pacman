import {Map, Point} from 'game-idl';

export default interface MapService {

    getMap(): Map;

    getTilesPositions(): Point[];

    getRandomPoint(): Point;

}
