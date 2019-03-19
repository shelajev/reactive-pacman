import { Scene } from 'phaser';
import CompassConfig from './CompassConfig';
import CompassService from './CompassService';

export default class CompassScene extends Scene {
    private service: CompassService;
    private compass: Phaser.GameObjects.Image;
    private compassNeedle: Phaser.GameObjects.Image;

    constructor() {
        super('Compass');
    }

    create(config: CompassConfig) {
        this.compass = this.add.image(60, 60, 'compass').setScale(0.6 * config.config.scale);
        this.compassNeedle = this.add.image(60, 60, 'compass-needle').setScale(0.6 * config.config.scale);
        this.service = new CompassService(config.playerService, config.locationService, config.state);

        // this.scaleChildren(config.scale);
    }

    destroy() {
        this.service.dispose()
    }

    scaleChildren(scale: any) {
        var children = this.children.list;
        for (var i = 0 ; i < children.length ; i++) {
            children[i].x *= scale;
            children[i].y *= scale;
        }
    }

    update() {
        const { rotation } = this.service;
        // this.scaleChildren(1 / this.sizeData.scale);

        // Object.keys(this.players).forEach(function(key:any, index) {
            // if (self.playersSprites[key].player.type == "man") {
                // var dist = self.distance2(self.player, self.players[key]);
                // if (lowestDist == -1 || dist < lowestDist) {
                    // lowestDist = dist;
                    // closestEnemy = self.players[key];
                // }
            // }
        // });

        if (rotation == undefined) {
            this.compassNeedle.setRotation((Date.now() / 350) % 360);
        }
        else {
            this.compassNeedle.setRotation(rotation + Math.PI / 2 + Math.PI);
        }

        // this.scaleChildren(this.sizeData.scale);
    }
}