import { Point } from '@shared/point_pb';

function distance(p1: Point, p2: Point): number {
    let d1 = p1.getX() - p2.getY();
    let d2 = p1.getY()- p2.getY();
    return d1 * d1 + d2 * d2;
}

export default distance;
