import GameState from "./GameState";
import GameConfig from "./GameConfig";
import ExtraService from "../api/ExtrasService";
import { Extra } from "@shared/extra_pb";
import * as FastBitSet from 'fastbitset';
import SceneSupport from "../Commons/SceneSupport";

export default class ExtrasManager implements SceneSupport {
    extra: FastBitSet;
    extraSprites: Map<number, Phaser.Physics.Arcade.Sprite>;

    constructor(
        private scene: Phaser.Scene,
        private state: GameState,
        private config: GameConfig,
        extras: Array<number>,
        extraService: ExtraService,
    ) {
        extras.forEach(extra => this.insertExtra(extra))
        extraService.extras()
            .consume(e => this.doOnExtra(e));
    }

    doOnExtra(extra: Extra.AsObject) {
        this.retainExtra(extra.last);
        this.insertExtra(extra.current);
    }

    retainExtra(position: number) {
        if (this.extra.has(position)) {
            this.extra.remove(position);

            const normalizedPosition = Math.abs(position);

            this.extraSprites.get(normalizedPosition).destroy();
            this.extraSprites.delete(normalizedPosition);
        }
    }

    insertExtra(position: number) {
        const normalizedPosition = Math.abs(position);
        const { width, height, size, scale } = this.config;
        const i = normalizedPosition % width;
        const j = Math.floor(normalizedPosition / width);
        const sprite = this.scene.physics.add
            .sprite(
                i * size,
                j * size,
                'food' + (Math.sign(position) === 1 ? '1' : '2')
            )
            .setScale(scale);
        this.extraSprites.set(normalizedPosition, sprite);
        this.extra.add(position);
    }

    update(): void { }
}