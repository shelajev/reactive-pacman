import RSocketWebSocketClient from 'rsocket-websocket-client';
import { Scene, Game } from 'phaser';
import { RpcClient } from 'rsocket-rpc-core';
import { BufferEncoders } from 'rsocket-core';
import { ReactiveSocket } from 'rsocket-types';
import { RSocketRPCServices } from 'game-idl';
import { Map } from 'game-idl';
import { GameScene } from './Game';
import Menu from './menu';
import { CompassScene } from './Compass';

import * as $ from 'jquery';
import * as RSocketApi from './api/rsocket';
import * as HttpApi from './api/http';
import * as GrpcApi from './api/grpc';

export class Boot extends Scene {

    constructor() {
        super('Boot');
    }

    preload() {
        
        this.load.image('logo', 'asset/logo.png');

        this.load.image('food1', 'asset/food1.png');
        this.load.image('food2', 'asset/food2.png');

        this.load.image('compass', 'asset/compass.png');
        this.load.image('compass-needle', 'asset/compass-needle.png');

        this.load.spritesheet('ghost', 'asset/ghost2.png', {frameWidth: 60, frameHeight: 60});
        this.load.spritesheet('man', 'asset/pacman-sprite.png', {frameWidth: 60, frameHeight: 60});

        this.load.spritesheet('tiles', 'asset/tile2.png', {frameWidth: 100, frameHeight: 100});
    }

    create(config : any) {
        const urlParams = new URLSearchParams(window.location.search);
        const type = urlParams.get('type');
        if(type === "rsocket") {
            let rSocket: ReactiveSocket<any, any>;
            const client = new RpcClient({
                transport: new RSocketWebSocketClient(
                    {
                        url: urlParams.get('endpoint') || 'ws://localhost:3000',
                    },
                    BufferEncoders
                ),
                setup: {
                    keepAlive: 5000,
                    lifetime: 60000,
                },
                responder: new RSocketRPCServices.MapServiceServer({
                    setup: (map: Map) => {
                        this.scene.start('Menu', { sizeData: config, maze: map.toObject(), playerService: new RSocketApi.PlayerServiceClientSharedAdapter(rSocket), extrasService: new RSocketApi.ExtrasServiceClientAdapter(rSocket), gameService: new RSocketApi.GameServiceClientAdapter(rSocket) });
                    }
                })
            });


            this.showLoadingCircle(() =>
                client
                    .connect()
                    .then(rsocket => {
                        console.log(rsocket);
                        rSocket = rsocket;
                    })
            );
        } else if (type === "grpc") {

            this.showLoadingCircle(() =>
            new GrpcApi.SetupServiceClientAdapter()
            .map()
                .then(map => this.scene.start('Menu', { sizeData: config, maze: map, playerService: new GrpcApi.PlayerServiceClientSharedAdapter(), extrasService: new GrpcApi.ExtrasServiceClientAdapter(), gameService: new GrpcApi.GameServiceClientAdapter() }))
            );
        } else {
            this.showLoadingCircle(() =>
                new HttpApi.SetupServiceClientAdapter()
                    .map()
                    .then(map => this.scene.start('Menu', { sizeData: config, maze: map, playerService: new HttpApi.PlayerServiceClientSharedAdapter(), extrasService: new HttpApi.ExtrasServiceClientAdapter(), gameService: new HttpApi.GameServiceClientAdapter() }))
            );
        }
    }

    showLoadingCircle(callback: () => void) {
        $('#phaser-overlay-container').css("pointer-events", "none");
        $('#phaser-overlay-container').show();
        $('#phaser-overlay-container #phaser-overlay').children().hide();
        $(".main").hide();
        $("#phaser-container").css("background-color", "white");
        $('#phaser-overlay-container #phaser-overlay').find('.loader').fadeIn(200, callback);
    }
}

(() => {
    const normalWidth = 1280;
    const normalHeight = 720;
    const scale: number = 1;
    const zoom = 1;
    const game = new Game({
        type: Phaser.AUTO,
        parent: 'canvas-container',
        backgroundColor: '#116',
        width: normalWidth * zoom * scale,
        height: normalHeight * zoom * scale,
        physics: {
            default: 'arcade',
            arcade: {
                debug: false
            }
        },
        scale: {
            mode: Phaser.Scale.FIT,
            autoCenter: Phaser.Scale.CENTER_BOTH
        },
        scene: [Boot, Menu, GameScene, CompassScene],
        // scene: [Boot, Menu, GameLoader, Game, Compass]
    });
    const sizeData = {
        width: normalWidth * scale,
        height: normalHeight * scale,
        scale: scale,
        zoom: zoom
    };
    game.scene.start('Boot', sizeData);
})();

