
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

    get actor(): Phaser.Physics.Arcade.Sprite {
        return
    }

    create(config: { maze: any; extras: number[]; quadrantMode: any; sizeData: any; player: Player.AsObject; players: Player.AsObject[]; playerService: PlayerService; extrasService: ExtrasService; }) {
        this.config = {
            zoom: config.sizeData.zoom,
            size: 100,
            width: config.sizeData.width,
            height: config.sizeData.height,
            scale: config.sizeData.scale
        }

        this.state = {
            tiles: config.maze.tilesList,
            player: config.player,
            players: config.players.reduce<{ [key: string]: Player.AsObject }>((players, player) => (players[player.uuid] = player) && players, {}),
            powerState: 0
        }

        var self = this;

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
            frames: this.anims.generateFrameNumbers('man', { start: 0, end: 1 }),
            frameRate: 5,
            repeat: -1
        });

        var directionStates = ["", "-up", "-down"];

        for (var i = 0; i < directionStates.length; i++) {
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

        var fadeTween = this.tweens.add({
            targets: this.text,
            alpha: 0,
            duration: 500,
            delay: 3000,
            ease: 'Power1',
            repeat: 0
        })

        this.cameras.main.setSize(this.config.width * this.config.zoom, this.config.height * this.config.zoom);
        //this.cameras.main.setBackgroundColor("#ff0000");

        // this.socket.on('user connected', this.addPlayer.bind(this));

        // this.socket.on('user position', function(user) {
        //     self.players[user.uuid].motionPath.push({
        //         x: user.x,
        //         y: user.y,
        //         direc: user.direc
        //     });
        //     self.players[user.uuid].x = user.x;
        //     self.players[user.uuid].y = user.y;
        //     self.players[user.uuid].rotation = user.rotation;
        //     self.players[user.uuid].flipX = user.flipX;
        //     self.players[user.uuid].prevDirec = self.players[user.uuid].direc;
        //     self.players[user.uuid].direc = user.direc;

        //     var key = user.uuid;
        //     self.players[key].sprite.setRotation(self.players[key].rotation);
        //     self.players[key].sprite.setFlipX(self.players[key].flipX);
        //     //self.players[key].sprite.x = self.players[key].x;
        //     //self.players[key].sprite.y = self.players[key].y;

        //     //self.players[key].text.x = self.players[key].x;
        //     //self.players[key].text.y = self.players[key].y + self.textOffset;

        //     var p = self.players[key];
        //     var now = Date.now();
        //     var dt = now - p.time;
        //     p.time = now;
        // });

        // this.socket.on('user disconnected', function(uuid) {
        //     self.players[uuid].sprite.destroy();
        //     self.players[uuid].text.destroy();
        //     delete self.players[uuid];
        // });

        // this.socket.on('score', function(score) {
        //     self.setScore(score);
        // });

        // this.socket.on('leaderboard', function(leaderboard) {
        //     self.setLeaderboard(leaderboard);
        // });

        // this.socket.on('powerup', function(time) {
        //     var anim = "";
        //     if (time > 0) {
        //         var wearoffSec = 3;
        //         if (time > wearoffSec * 1000) {
        //             anim = "powerup";
        //         }
        //         else {
        //             anim = "powerup-wearoff";
        //         }
        //     }
        //     else {
        //         anim = "default";
        //     }

        //     if (self.powerupState != anim && self.powerupState == "default" && (anim == "powerup" || anim == "powerup-wearoff")) {
        //         if (config.playerType == "man") {
        //             self.notification("Powerup activated! You can kill ghosts.");
        //         }
        //         else if (config.playerType == "ghost") {
        //             self.notification("Dino Powerup activated! Dinos can now kill you.");
        //         }
        //     }

        //     self.powerupState = anim;
        // });

        // this.time = Date.now();

        this.scaleChildren(this.config.scale);
        this.managers = [
            new PlayersManager(this, this.state, this.config, config.playerService, new KeysService(this)),
            new MapManager(this, this.state, this.config),
            new ExtrasManager(this, this.state, this.config, config.extras, config.extrasService),
        ]
    }

    scaleChildren(scale: any) {
        var children = this.children.list;
        for (var i = 0; i < children.length; i++) {
            children[i].x *= scale;
            children[i].y *= scale;
        }
    }

    update() {
        this.scaleChildren(1 / this.config.scale);

        this.managers.forEach(manager => manager.update());

        this.scaleChildren(this.config.scale);
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
        // this.setLeaderboard([]);
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

    // setLeaderboard(data: any) {
    //     var overlay = this.overlay;
    //     var leaderboard = overlay.find(".leaderboard").find('ol');
    //     leaderboard.empty();
    //     for (var i = 0; i < data.length; i++) {
    //         var elem = $("<li></li>");
    //         elem.text(data[i].name + " - " + data[i].score);
    //         if (data[i].uuid == this.player.uuid) {
    //             elem.addClass('me');
    //         }
    //         elem.appendTo(leaderboard);
    //     }
    // }

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