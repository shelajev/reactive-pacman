import { Map } from '@shared/map_pb';
import { Point } from '@shared/point_pb';

export interface MapService {

    getMap(): Map;

    getTilesPositions(): Point[];

    getRandomPoint(): Point;

}
