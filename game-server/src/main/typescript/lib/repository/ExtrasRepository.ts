export interface ExtrasRepository {

    collideExtra(x: number, y: number): number;

    createExtra(size: number): number;

    findAll(): number[];

    saveAll(extras: number[]): void;
}
