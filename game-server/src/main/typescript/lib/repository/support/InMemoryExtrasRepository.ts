import ExtrasRepository from '../ExtrasRepository';

export default class InMemoryExtrasRepository implements ExtrasRepository {

    tileSize = 100;
    mapHeight = 60;
    mapWidth = 60;
    offset = 11;

    bitmap: Set<number> = new Set();

    collideExtra(x: number, y: number): number {
        let i = Math.round(x / this.tileSize);
        let j = Math.round(y / this.tileSize);

        const flattenPosition = i + j * this.mapWidth;
        if (this.bitmap.has(flattenPosition)) {
            this.bitmap.delete(flattenPosition);
            return flattenPosition;
        } else if (this.bitmap.has(-flattenPosition)) {
            this.bitmap.delete(flattenPosition);
            return -flattenPosition;
        }

        return 0;
    }


    
    createExtra(size: number): number {
        let nextPosition = 0;
        do {
            nextPosition = InMemoryExtrasRepository.randomPosition(this.mapWidth, this.mapHeight, this.offset);
        } while (this.bitmap.has(nextPosition) || this.bitmap.has(-nextPosition));

        let powerupCount = 0;
        this.bitmap.forEach(el => Math.sign(el) < 0 ? powerupCount++ : {});

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
            this.bitmap.add(-nextPosition);
            return -nextPosition;
        }
        else {
            this.bitmap.add(nextPosition);
            return nextPosition;
        }
    }

    findAll(): Set<number> {
        return this.bitmap;
    }

    saveAll(extras: number[]): void {
        extras.forEach(extra => {
            this.bitmap.add(extra);
        });
        console.log('extras', this.bitmap);
    }


    static randomPosition(width: number, height: number, offset: number): number {
        return (Math.floor(Math.random() * (width - 2 * offset + 1) + offset) +  Math.floor(Math.random() * (height - 2 * offset + 1) + offset) * height);
    }
}
