import { ExtrasRepository } from '../ExtrasRepository';

export class InMemoryExtrasRepository implements ExtrasRepository {

    tileSize = 100;
    mapHeight = 60;
    mapWidth = 60;
    offset = 11;

    bitmap: number[] = [];

    collideExtra(x: number, y: number): number {
        let i = Math.round(x / this.tileSize);
        let j = Math.round(y / this.tileSize);

        const flattenPosition = i + j * this.mapWidth;
        this.bitmap[flattenPosition] = 0;
        if (this.bitmap[flattenPosition] === 1) {
            return flattenPosition;
        } else if (this.bitmap[flattenPosition] === -1) {
            return -flattenPosition;
        }

        return 0;
    }

    createExtra(size: number): number {
        let nextPosition = 0;
        do {
            nextPosition = InMemoryExtrasRepository.randomPosition(this.mapWidth, this.mapHeight, this.offset);
        } while (this.bitmap[nextPosition] !== 0);

        const powerupCount = this.bitmap.filter(v => v === -1).length;

        var powerupAllowed = false;
        var totalSpace =
            (this.mapWidth - 2 * this.offset + 1) * (this.mapHeight - 2 * this.offset + 1);
        if (size <= 2) {
            powerupAllowed = powerupCount < Math.ceil(totalSpace / 360);
        }
        else if (size <= 4) {
            powerupAllowed = powerupCount < Math.ceil(totalSpace / 900);
        }
        else {
            powerupAllowed = powerupCount == 0 && Math.random() < 0.02;
        }

        if (powerupAllowed) {
            this.bitmap[nextPosition] = -1;
            return -nextPosition;
        }
        else {
            this.bitmap[nextPosition] = 1;
            return nextPosition;
        }
    }

    findAll(): number[] {
        return this.bitmap;
    }

    saveAll(extras: number[]): void {
        extras.forEach(extra => {
            this.bitmap[extra] = 1;
        })
    }


    static randomPosition(width: number, height: number, offset: number): number {
        return (Math.floor(Math.random() * (width - 2 * offset + 1) + offset) +  Math.floor(Math.random() * (height - 2 * offset + 1) + offset) * height);
    }
}
