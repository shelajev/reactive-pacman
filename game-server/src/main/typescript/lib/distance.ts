import { Point } from '@shared/point_pb';

function distance(p1: Point.AsObject, p2: Point.AsObject): number {
    let d1 = p1.x - p2.x;
    let d2 = p1.y - p2.y;
    return d1 * d1 + d2 * d2;
}

export default distance;
