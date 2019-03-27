import MapService from '../MapService';
import { Map } from '@shared/map_pb';
import { Point } from '@shared/point_pb';
import { Size } from '@shared/size_pb';
import { Tile } from '@shared/tile_pb';

export default class DefaultMapService implements MapService {
    private map: Map
    private tilesPosition: Point[];

    constructor() {
        this.map = new Map();
        DefaultMapService.generate(60, 60, 10)
            .forEach((tile, i) => this.map.addTiles(tile, i));
        const size = new Size();
        size.setWidth(60);
        size.setHeight(60);
        this.map.setSize(size);
        this.tilesPosition = DefaultMapService.generateTileCoordinates(60, 60, 10, 10);
    }

     getMap(): Map {
        return this.map;
    }

    getTilesPositions(): Point[] {
        return this.tilesPosition;
    }

    getRandomPoint() {
        return this.tilesPosition[
            DefaultMapService.getRandomIntInclusive(0, this.getTilesPositions.length - 1)
        ];
    }

    static generateTileCoordinates(
        width: number,
        height: number,
        offsetX: number,
        offsetY: number
    ): Point[] {
        const minX = offsetX;
        const maxX = width - offsetX - 1;
        const minY = offsetY;
        const maxY = height - offsetY - 1;

        const arr: Point[] = [];

        for (let i = minX; i <= maxX; i++) {
            for (let j = minY; j <= maxY; j++) {
                const point = new Point();
                point.setX(i);
                point.setY(j);
                arr.push(point);
            }
        }
        return arr;
    }

    static distance2(p1: Point, p2: Point): number {
        const d1 = p1.getX() - p2.getX();
        const d2 = p1.getY() - p2.getY();
        return d1 * d1 + d2 * d2;
    }

    static generate(width: number, height: number, offset: number): Tile[] {
        const arr = [];
        for (let i = 0; i < width; i++) {
            const column: number[] = [];
            column.fill(-1, 0, height);
            arr[i] = column;
        }
        const tileIndices = DefaultMapService.generateTileCoordinates(width, height, offset, offset);
        const center = new Point();
        center.setX(Math.round(width / 2));
        center.setY(Math.round(height / 2));

        tileIndices.sort(
            (p1, p2) => this.distance2(p1, center) > this.distance2(p2, center) ? -1 : 1
        );

        let count = 0;
        const bounds = offset;
        for (let p = 0; p < tileIndices.length; p++) {
            const point = tileIndices[p];
            const i = point.getX();
            const j = point.getY();

            if (!arr[i]) {
                arr[i] = [];
            }

            if (arr[i][j] === -1) {
                const shapeNum = count;
                arr[i][j] = shapeNum;
                let shapeCount = 0;
                let offsetX = 0;
                let offsetY = 0;
                const max = DefaultMapService.getRandomIntInclusive(4, 8);
                while (shapeCount < max) {
                    let direc = DefaultMapService.getRandomIntInclusive(0, 3);
                    let changeX = 0;
                    let changeY = 0;
                    let direcCount = 0;
                    do {
                        if (direc === 0) {
                            changeX = 1;
                        } else if (direc === 1) {
                            changeX = -1;
                        } else if (direc === 2) {
                            changeY = 1;
                        } else {
                            changeY = -1;
                        }
                        const elem = arr[i + offsetX + changeX][j + offsetY + changeY];
                        if (!elem || elem === -1) {
                            offsetX += changeX;
                            offsetY += changeY;
                            arr[i + offsetX][j + offsetY] = shapeNum;
                            break; 
                        } else {
                            changeX = 0;
                            changeY = 0;
                            direc++;
                            if (direc === 4) {
                                direc = 0;
                            }
                            direcCount++;
                        }
                    } while (changeX === 0 && changeY === 0 && direcCount < 4);
                    shapeCount++;
                }
                count++;
            }
        }
        return DefaultMapService.generateTiles(arr, width, height, offset);
    }

    static generateTiles(data: number[][], width: number, height: number, offset: number): Tile[] {
        const tiles: TileInner[] = [];
        for (let i = 0; i < data.length - 1; i++) {
            for (let j = 0; j < data[i].length - 1; j++) {
                const walls = [false, false, false, false];
                if (data[i][j] === data[i + 1][j] && data[i][j] !== null && data[i + 1][j] !== null) {
                    walls[0] = true;
                }
                if (data[i][j] === data[i][j + 1] && data[i][j] !== null && data[i][j + 1] !== null) {
                    walls[1] = true;
                }
                if (data[i][j + 1] === data[i + 1][j + 1] && data[i][j + 1] !== null && data[i + 1][j + 1] !== null) {
                    walls[2] = true;
                }
                if (data[i + 1][j] === data[i + 1][j + 1] && data[i + 1][j] !== null && data[i + 1][j + 1] !== null) {
                    walls[3] = true;
                }
                const tile = new TileInner();
                tile.x = i;
                tile.y = j;
                tile.walls = walls;
                tiles.push(tile);
            }
        }
        DefaultMapService.checkTiles(tiles, width, height, offset);

        return tiles.map(t => {
            const point = new Point();
            point.setX(t.x);
            point.setY(t.y);
            const tile = new Tile();
            tile.setPoint(point);
            t.walls.forEach((wall, i) => tile.addWalls(wall, i));
            return tile;
        });
    }

    static checkTiles(tiles: TileInner[], width: number, height: number, offset: number) {
        const minX = offset;
        const maxX = width - offset - 1;
        const minY = offset;
        const maxY = height - offset - 1;
        const bounds = [minX, maxX, minY, maxY];
        let badTiles: TileInner[] = [];
        while (true) {
            DefaultMapService.resetTileCheck(bounds, tiles);
            DefaultMapService.checkTileRecursive(minX, minY, bounds, tiles);
            badTiles = DefaultMapService.getBadTiles(bounds, tiles);
            if (badTiles.length === 0) {
                break;
            }
            let randomTile = 0;
            let randomDirec = 0;
            let neighbour: TileInner;
            do {
                randomTile = DefaultMapService.getRandomIntInclusive(0, badTiles.length - 1);
                randomDirec = DefaultMapService.getRandomIntInclusive(0, 3);
                neighbour = DefaultMapService.getNeighbour(badTiles[randomTile], randomDirec, tiles);
            } while (
                !neighbour.checked || neighbour.x < minX ||
                neighbour.x > maxX || neighbour.y < minY ||
                neighbour.y > maxY
            );
            DefaultMapService.removeWall(badTiles[randomTile], randomDirec, tiles);
        }
    }

    static removeWall(tile: TileInner, direction: number, tiles: TileInner[]): void {
        tile.walls[direction] = false;
        DefaultMapService.getNeighbour(tile, direction, tiles).walls[(direction + 2) % 4] = false;
    }

    static getNeighbour(tile: TileInner, direction: number, tiles: TileInner[]): TileInner {
        let x = tile.x;
        let y = tile.y;
        if (direction === 0) {
            y -= 1;
        } else if (direction === 1) {
            x -= 1;
        } else if (direction === 2) {
            y += 1;
        } else {
            x += 1;
        }

        return DefaultMapService.getTile(x, y, tiles);
    }

    static checkTileRecursive(x: number, y: number, bounds: number[], tiles: TileInner[]): void {
        const tile = DefaultMapService.getTile(x, y, tiles);
        tile.checked = true;
        if (x !== bounds[0] && !tile.walls[1] && !DefaultMapService.getTile(x - 1, y, tiles).checked) {
            DefaultMapService.checkTileRecursive(x - 1, y, bounds, tiles);
        }
        if (x !== bounds[1] && !tile.walls[3] && !DefaultMapService.getTile(x + 1, y, tiles).checked) {
            DefaultMapService.checkTileRecursive(x + 1, y, bounds, tiles);
        }
        if (x !== bounds[2] && !tile.walls[0] && !DefaultMapService.getTile(x, y - 1, tiles).checked) {
            DefaultMapService.checkTileRecursive(x, y - 1, bounds, tiles);
        }
        if (x !== bounds[3] && !tile.walls[2] && !DefaultMapService.getTile(x, y + 1, tiles).checked) {
            DefaultMapService.checkTileRecursive(x, y + 1, bounds, tiles);
        }
    }

    static getTile(x: number, y: number, tiles: TileInner[]): TileInner {
        for (let i = 0; i < tiles.length; i++) {
            const tile: TileInner = tiles[i];
            if (tile.x === x && tile.y === y) {
                return tile;
            }
        }
    }

    static resetTileCheck(bounds: number[], tiles: TileInner[]): void {
        for (let i = bounds[0]; i <= bounds[1]; i++) {
            for (let j = bounds[2]; j <= bounds[3]; j++) {
                const tile = DefaultMapService.getTile(i, j, tiles);
                tile.checked = false;
            }
        }
    }

    static getBadTiles(bounds: number[], tiles: TileInner[]) {
        const badTiles: TileInner[] = [];

        for (let i = bounds[0]; i <= bounds[1]; i++) {
            for (let j = bounds[2]; j <= bounds[3]; j++) {
                const tile = DefaultMapService.getTile(i, j, tiles);
                if (!tile.checked) {
                    badTiles.push(tile);
                }
            }
        }
        return badTiles;
    }

    static getRandomIntInclusive(min: number, max: number) {
        return (Math.ceil(Math.random() * (max - min)) + min);
    }
}


class TileInner {

    x: number;
    y: number;
    checked: boolean;
    walls: boolean[]
}

