export default interface ExtrasRepository {

    collideExtra(x: number, y: number): number;

    createExtra(size: number): number;

    findAll(): Set<number>;

    saveAll(extras: number[]): void;
}
