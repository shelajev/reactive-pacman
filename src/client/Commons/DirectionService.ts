import { Flux, DirectProcessor } from "reactor-core-js/flux";
import { Direction } from "@shared/location_pb";

export interface DirectionService {

    listen(): Flux<Direction>
}

export class KeysService implements DirectionService {

    private keysProcessor: DirectProcessor<Direction>;

    constructor(scene: Phaser.Scene) {
        this.keysProcessor = new DirectProcessor<Direction>();
        scene.input.keyboard.on('keydown', ({ key }: any) => {
            switch (key) {
                case "ArrowUp":
                    this.keysProcessor.onNext(Direction.UP);
                    return;

                case "ArrowLeft":
                    this.keysProcessor.onNext(Direction.LEFT);
                    return;

                case "ArrowDown":
                    this.keysProcessor.onNext(Direction.DOWN);
                    return;
                case "ArrowRight":
                    this.keysProcessor.onNext(Direction.RIGHT);
                    return;
            }
        }, this);
    }

    listen(): Flux<Direction> {
        return this.keysProcessor;
    }
}



// this.swipeDirec = -1;

// this.swipeData = {
//     startPosition: null,
//     timestamp: null
// };
// if (!this.quadrantMode) {
//     this.input.on("pointerup", this.endSwipe, this);
//     this.input.addMoveCallback((e: any) => {
//         if (self.scene.isActive("Game")) {
//             var x = e.touches[0].clientX;
//             var y = e.touches[0].clientY;
//             if (!self.swipeData.startPosition) {
//                 self.swipeData.startPosition = {
//                     x: x,
//                     y: y
//                 };
//                 self.swipeData.timestamp = Date.now();
//             }
//             else if (self.swipeData.timestamp + 500 > Date.now()) {

//                 var dx = x - self.swipeData.startPosition.x;
//                 var dy = y - self.swipeData.startPosition.y;
//                 var swipe = new Phaser.Geom.Point(dx, dy);
//                 var swipeMagnitude = Phaser.Geom.Point.GetMagnitude(swipe);
//                 var swipeNormal = new Phaser.Geom.Point(swipe.x / swipeMagnitude, swipe.y / swipeMagnitude);
//                 if(swipeMagnitude > 15 && (Math.abs(swipeNormal.y) > 0.6 || Math.abs(swipeNormal.x) > 0.6)) {
//                     if(swipeNormal.x > 0.6) {
//                         self.swipeDirec = 3
//                     }
//                     if(swipeNormal.x < -0.6) {
//                         self.swipeDirec = 1;
//                     }
//                     if(swipeNormal.y > 0.6) {
//                         self.swipeDirec = 2;
//                     }
//                     if(swipeNormal.y < -0.6) {
//                         self.swipeDirec = 0;
//                     }
//                     self.swipeData.startPosition = null;
//                 }
//             }
//             else {
//                 self.swipeData.startPosition = null;
//             }
//         }
//     });
// }