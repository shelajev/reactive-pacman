import { Flux, DirectProcessor } from "reactor-core-js/flux";
import { Direction } from "game-idl";

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
export class SwipeService implements DirectionService {

    private swipeProcessor: DirectProcessor<Direction>;
    private initialX: number;
    private initialY: number;

    constructor(scene: Phaser.Scene) {
        const canvas = window.document.querySelector('canvas');
        this.swipeProcessor = new DirectProcessor<Direction>()
        window.document.querySelector('#phaser-overlay').addEventListener("touchstart", this.startTouch.bind(this), false);
        window.document.querySelector('#phaser-overlay').addEventListener("touchmove", this.moveTouch.bind(this), false);
        // window.document.querySelector('#phaser-overlay').ontouchstart = (e) => this.startTouch(e);
        // window.document.querySelector('#phaser-overlay').ontouchmove = (e) => this.moveTouch(e);
    }

    startTouch(e: TouchEvent) {
        this.initialX = e.touches[0].clientX;
        this.initialY = e.touches[0].clientY;
        e.preventDefault();

    }

    moveTouch(e: TouchEvent) {
        if (this.initialX === null) {
            return;
        }
        var currentX = e.touches[0].clientX;
        var currentY = e.touches[0].clientY;
        
        var diffX = this.initialX - currentX;
        var diffY = this.initialY - currentY;
        
        if (Math.abs(diffX) > Math.abs(diffY)) {
            // sliding horizontally
            if (diffX > 0) {
            // swiped left
                this.swipeProcessor.onNext(Direction.LEFT);
            } else {
            // swiped right
                this.swipeProcessor.onNext(Direction.RIGHT);
            }
        } else {
            // sliding vertically
            if (diffY > 0) {
            // swiped up
                this.swipeProcessor.onNext(Direction.UP);
            } else {
            // swiped down
                this.swipeProcessor.onNext(Direction.DOWN);
            }
        }

        this.initialX = null;
        this.initialY = null;

        e.preventDefault();
    }


    listen(): Flux<Direction> {
        return this.swipeProcessor;
    }
}


export class ControlsService implements DirectionService {
    private swipeService: SwipeService;
    private keysService: KeysService;

    constructor(scene: Phaser.Scene) {
        this.keysService = new KeysService(scene);
        this.swipeService = new SwipeService(scene);
    }

    listen(): Flux<Direction> {
        return Flux.mergeArray([this.swipeService.listen(), this.keysService.listen()]);
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