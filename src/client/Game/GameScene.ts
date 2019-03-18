
import { Player } from '../../shared/player_pb';
import * as $ from 'jquery';
import GameConfig from './GameConfig';
import PlayerService from "../api/PlayerService";
import ExtrasService from "../api/ExtrasService";
import GameState from './GameState';
import SceneSupport from '../Commons/SceneSupport';
import PlayersManager from './PlayersManager';
import MapManager from './MapManager';
import ExtrasManager from './ExtrasManager';
import { KeysService } from '../Commons/DirectionService';
import Maze from 'src/server/maze';
import LeaderboardManager from './LeaderboardManager';

export default class GameScene extends Phaser.Scene {
    overlay: JQuery<HTMLElement>;
    config: GameConfig;
    state: GameState;
    text: any;

    managers: SceneSupport[];

    constructor() {
        super('Game');

        this.overlay = $("#phaser-overlay");
    }

    create(config: { maze: any; extras: number[]; quadrantMode: any; sizeData: any; player: Player.AsObject; players: Player.AsObject[]; playerService: PlayerService; extrasService: ExtrasService; }) {
        const self = this;

        this.config = {
            zoom: config.sizeData.zoom,
            size: 100,
            map: {
                width: config.maze.size.width,
                height: config.maze.size.height
            },
            screen: {
                width: config.sizeData.width,
                height: config.sizeData.height,
            },
            scale: config.sizeData.scale
        };

        this.state = {
            tiles: config.maze.tilesList,
            player: config.player,
            players: config.players.reduce<{ [key: string]: Player.AsObject }>((players, player) => (players[player.uuid] = player) && players, {}),
            powerState: 0
        };

        setTimeout(function () {
            if (config.player.type == Player.Type.PACMAN) {
                self.notification("Avoid ghosts to survive!");
                setTimeout(function () {
                    self.notification("Eat food to gain points.");
                }, 10000);
            }
            else if (config.player.type == Player.Type.GHOST) {
                self.notification("Kill dinos to gain points!");
                setTimeout(function () {
                    self.notification("Use your compass to track dinos.");
                }, 10000);
            }
        }, 3000);

        this.anims.create({
            key: 'eat',
            frames: this.anims.generateFrameNumbers('man', { start: 0, end: 3 }),
            frameRate: 10,
            repeat: -1
        });

        const directionStates = ["", "-up", "-down"];

        for (let i = 0; i < directionStates.length; i++) {
            this.anims.create({
                key: ("default" + directionStates[i]) + "",
                frames: this.anims.generateFrameNumbers('ghost', { frames: [0 + i] }),
                frameRate: 5,
                repeat: 0
            });

            this.anims.create({
                key: ("powerup" + directionStates[i]) + "",
                frames: this.anims.generateFrameNumbers('ghost', { frames: [3 + i] }),
                frameRate: 5,
                repeat: 0
            });

            this.anims.create({
                key: ("powerup-wearoff" + directionStates[i]) + "",
                frames: this.anims.generateFrameNumbers('ghost', { frames: [3 + i, 6 + i] }),
                frameRate: 5,
                repeat: -1
            });
        }

        const fadeTween = this.tweens.add({
            targets: this.text,
            alpha: 0,
            duration: 500,
            delay: 3000,
            ease: 'Power1',
            repeat: 0
        });

        this.cameras.main.setSize(this.config.screen.width * this.config.zoom, this.config.screen.height * this.config.zoom);


        this.managers = [
            new MapManager(this, this.state, this.config),
            new PlayersManager(this, this.state, this.config, config.playerService, new KeysService(this)),
            new ExtrasManager(this, this.state, this.config, config.extras, config.extrasService),
            new LeaderboardManager(this, this.state, this.config, config.playerService)
        ]
        // this.scaleChildren(this.config.scale);
    }

    scaleChildren(scale: any) {
        var children = this.children.list;
        for (var i = 0; i < children.length; i++) {
            children[i].x *= scale;
            children[i].y *= scale;
        }
    }

    update(time: number, deltaTime: number) {
        // this.scaleChildren(1 / this.config.scale);

        this.managers.forEach(manager => manager.update(time, deltaTime));

        // this.scaleChildren(this.config.scale);
    }

    // getPlayerText(user: any) {
    //     var text = this.add.text(user.x, user.y + this.textOffset, user.nickname, { fontFamily: 'Arial', fontSize: '18px', fill: 'rgba(255,255,255,0.8)' });
    //     text.setScale(this.sizeData.scale);
    //     text.setOrigin(0.5);
    //     return text;
    // }


    initOverlay(config: any) {
        $('#phaser-overlay-container').show();
        $('#phaser-overlay-container #phaser-overlay').children().show();
        $('#phaser-overlay-container #phaser-overlay').find('.loader').hide();
        $(".login").hide();
        $(".main").hide();
        var overlay = this.overlay;
        overlay.find('.notification-tray').empty();
        this.setScore(config.score);
        this.setLeaderboard();
    }

    hideOverlay() {
        $('#phaser-overlay-container').hide();
        $('#phaser-overlay-container #phaser-overlay').children().hide();
    }

    toggleLoader() {

    }

    setScore(score: any) {
        var overlay = this.overlay;
        overlay.find(".score").find("p").text("Score: " + score);
    }

    setLeaderboard() {
        var overlay = this.overlay;
        var leaderboard = overlay.find(".leaderboard").find('ol');
        leaderboard.empty();
    }

    notification(text: string) {
        var overlay = this.overlay;
        var target = overlay.find(".notification-tray");
        var elem = $("<div></div>");
        var p = $("<p></p>");
        p.text(text);
        p.appendTo(elem);
        elem.prependTo(target);
        elem.hide();
        var fadeTime = 500;
        var readTime = 7000;
        elem.fadeIn(fadeTime, function () {
            setTimeout(function () {
                elem.fadeOut(fadeTime, function () {
                    elem.remove();
                });
            }, readTime);
        });
    }

    // endSwipe(e: any) {
    //     this.swipeData.startPosition = null;

    //     // var swipeTime = e.upTime - e.downTime;
    //     // var swipe = new Phaser.Geom.Point(e.upX - e.downX, e.upY - e.downY);
    //     // var swipeMagnitude = Phaser.Geom.Point.GetMagnitude(swipe);
    //     // var swipeNormal = new Phaser.Geom.Point(swipe.x / swipeMagnitude, swipe.y / swipeMagnitude);
    //     // if(swipeMagnitude > 20 && swipeTime < 1000 && (Math.abs(swipeNormal.y) > 0.6 || Math.abs(swipeNormal.x) > 0.6)) {
    //     //     if(swipeNormal.x > 0.6) {
    //     //         this.swipeDirec = 3
    //     //     }
    //     //     if(swipeNormal.x < -0.6) {
    //     //         this.swipeDirec = 1;
    //     //     }
    //     //     if(swipeNormal.y > 0.6) {
    //     //         this.swipeDirec = 2;
    //     //     }
    //     //     if(swipeNormal.y < -0.6) {
    //     //         this.swipeDirec = 0;
    //     //     }
    //     // }
    // }
}