import { Scene } from 'phaser';
import Config from './Config';
import Model from './Model';

export default class Compass extends Scene {
    private model: Model;
    private compass: Phaser.GameObjects.Image;
    private compassNeedle: Phaser.GameObjects.Image;

    constructor() {
        super('Compass');
    }

    init(config : Config) {
        this.model = config.model;
    }

    create(config : Config) {
        this.compass = this.add.image(60, 60, 'compass').setScale(0.6 * config.scale);
        this.compassNeedle = this.add.image(60, 60, 'compass-needle').setScale(0.6 * config.scale);

        this.scaleChildren(config.scale);
    }

    // distance2(p1: Phaser.Physics.Arcade.Sprite, p2: Phaser.Physics.Arcade.Sprite) {
    //     var d1 = p1.x - p2.x;
    //     var d2 = p1.y - p2.y;
    //     return d1 * d1 + d2 * d2;
    // }

    scaleChildren(scale: any) {
        var children = this.children.list;
        for (var i = 0 ; i < children.length ; i++) {
            children[i].x *= scale;
            children[i].y *= scale;
        }
    }

    update() {
        const { rotation } = this.model;
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
            this.compassNeedle.setRotation(Date.now() % 360);
        }
        else {
            this.compassNeedle.setRotation(rotation);
        }

        // this.scaleChildren(this.sizeData.scale);
    }
}