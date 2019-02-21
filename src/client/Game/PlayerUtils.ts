import GameConfig from "./GameConfig";
import { Player } from "@shared/player_pb";
import { Location, Direction } from "@shared/location_pb";
import { Tile } from "@shared/tile_pb";
import { Point } from "@shared/point_pb";
import GameState from "./GameState";

export const updateSprite = (
    {location: { direction, position: {x, y} }, type }: Player.AsObject,
    tileSize: number, 
    playerSprite: Phaser.Physics.Arcade.Sprite
) => {

    playerSprite.setX(x * tileSize);
    playerSprite.setY(y * tileSize);

    if (direction === Direction.LEFT) {
        playerSprite.setFlipX(true);
        playerSprite.setRotation(Phaser.Math.DegToRad(0));
    }
    else if (direction === Direction.RIGHT) {
        playerSprite.setFlipX(false);
        playerSprite.setRotation(Phaser.Math.DegToRad(0));
    }
    else if (direction === Direction.UP) {
        playerSprite.setFlipX(false);
        if (type == Player.Type.PACMAN)
            playerSprite.setRotation(Phaser.Math.DegToRad(270));
    }
    else if (direction === Direction.DOWN) {
        playerSprite.setFlipX(false);
        if (type == Player.Type.PACMAN)
            playerSprite.setRotation(Phaser.Math.DegToRad(90));
    }
}

export const createSprite = (user: Player.AsObject, scene: Phaser.Scene, config: GameConfig, state: GameState): Phaser.Physics.Arcade.Sprite => {
    const { scale, size } = config;

    const sprite = scene.physics.add
        .sprite(
            user.location.position.x * size, 
            user.location.position.y * size,
            user.type == Player.Type.GHOST ? "ghost" : "man"
        )
        .setScale(scale);

    if (user.type == Player.Type.PACMAN) {
        sprite.anims.play("eat");
    }
    else if (user.type == Player.Type.GHOST) {
        sprite.anims.play(ghostAnimation(user.location.direction, state.powerState));
    }

    return sprite
}

export const ghostAnimation = (direction: Direction, powerState: number) => {
    let animationName = powerState == 0 ? "default" : powerState == 1 ? "powerup" : "powerup-wearoff";

    if (direction == Direction.UP) {
        animationName += "-up";
    }
    else if (direction == Direction.DOWN) {
        animationName += "-down";
    }

    return animationName;
}

export const playerSpeed = (playerType: Player.Type, dt: number) => {
    const maxSpeed = 5;
    const { powerState } = this.state;
    let speed = 0;

    if (playerType == Player.Type.PACMAN) {
        speed = (dt / 4);
    }
    else if (playerType == Player.Type.GHOST) {
        if (powerState == 0) {
            speed = (dt / 3.6);
        }
        else {
            speed = (dt / 4.5);
        }
    }

    speed = Math.min(speed, maxSpeed);

    return speed;
}

export const motionVector = (direction: number, speed: number) => {
    const obj = {
        x: 0,
        y: 0
    };

    if (direction === Direction.LEFT) {
        obj.x -= speed;
    }
    else if (direction === Direction.RIGHT) {
        obj.x += speed;
    }
    else if (direction === Direction.UP) {
        obj.y -= speed;
    }
    else if (direction === Direction.DOWN) {
        obj.y += speed;
    }

    return obj;
}


export const checkCollision = (
    tiles: Array<Tile.AsObject>,
    initialLocation: Location.AsObject,
    nextPosition: Point.AsObject,
    newDirection: boolean,
    regVec: { x: number; y: number; },
    forceTurn?: any
) => {
    const { direction } = initialLocation;
    // return {
    //     x: finalX,
    //     y: finalY,
    //     success: true
    // };

    var scaledX = nextPosition.x;
    var scaledY = nextPosition.y;
    var initialTileX = initialLocation.position.x;
    var initialTileY = initialLocation.position.x;
    var finalTileX = Math.round(scaledX);
    var finalTileY = Math.round(scaledY);
    var tilePath = [];

    if (direction == Direction.UP || direction == Direction.DOWN) {
        const multiplier = (direction == 0 ? -1 : 1)
        for (var i = 0; i <= Math.abs(initialTileY - finalTileY); i++) {
            var change = multiplier * i;
            tilePath.push({ x: initialTileX, y: initialTileY + change });
        }
    }
    else {
        for (var i = 0; i <= Math.abs(initialTileX - finalTileX); i++) {
            var change = (direction == 1 ? -1 : 1) * i;
            tilePath.push({ x: initialTileX + change, y: initialTileY });
        }
    }

    var threshold = 0.1;
    var extraWallSqueeze = .001;

    for (var t = 0; t < tilePath.length - 1; t++) {
        for (var i = 0; i < tiles.length; i++) {
            var tile = tiles[i];
            var tileX = tilePath[t].x;
            var tileY = tilePath[t].y;
            if (tileX == tile.point.x && tileY == tile.point.y) {
                var wall = tile.wallsList[direction];
                if (wall) {
                    if (direction == 0 || direction == 2) {
                        return {
                            x: nextPosition.x,
                            y: tileY,
                            //y: (direction == 0 ? tileY - threshold : tileY + threshold) * this.size,
                            success: false
                        };
                    }
                    else {
                        return {
                            //x: (direction == 1 ? tileX - threshold : tileX + threshold) * this.size,
                            x: tileX,
                            y: nextPosition.y,
                            success: false
                        };
                    }
                }
            }
        }
    }

    if (!newDirection) {
        threshold = 0;
    }

    for (var i = 0; i < tiles.length; i++) {
        var tile = tiles[i];
        var tileX = tilePath[tilePath.length - 1].x;
        var tileY = tilePath[tilePath.length - 1].y;
        if (tileX == tile.point.x && tileY == tile.point.y) {
            var wall = tile.wallsList[direction];

            var initX = initialTileX;
            var initY = initialTileY;

            var hypX = initX + regVec.x;
            var hypY = initY + regVec.y;

            var worked = false;
            if (wall && direction == 0 && scaledY >= tileY - threshold) {
                worked = true;
            }
            else if (wall && direction == 2 && scaledY <= tileY + threshold) {
                worked = true;
            }
            else if (wall && direction == 1 && scaledX >= tileX - threshold) {
                worked = true;
            }
            else if (wall && direction == 3 && scaledX <= tileX + threshold) {
                worked = true;
            }
            else if (!wall && (direction == 0 || direction == 2) && tileX >= Math.min(initX, hypX) && tileX <= Math.max(initX, hypX)) {
                worked = true;
            }
            else if (!wall && (direction == 1 || direction == 3) && tileY >= Math.min(initY, hypY) && tileY <= Math.max(initY, hypY)) {
                worked = true;
            }
            else if (forceTurn) {
                worked = true;
            }
            else {
                worked = false;
            }

            if (worked) {
                if (direction == 0 || direction == 2) {
                    return { x: tileX , y: scaledY, success: !wall || tilePath.length > 1 };
                }
                else {
                    return { x: scaledX, y: tileY, success: !wall || tilePath.length > 1 };
                }
            }
            else {
                var success = tilePath.length > 1;
                if (direction == 0 || direction == 2) {
                    return {
                        x: initialTileX,
                        y: tileY ,
                        success: success
                    };
                }
                else {
                    return {
                        x: tileX,
                        y: initialTileY,
                        success: success
                    };
                }
            }
        }
    }

    return {
        x: initialTileX,
        y: initialTileY,
        success: false
    };
}