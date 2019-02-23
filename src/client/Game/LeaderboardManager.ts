import PlayerService from "../api/PlayerService";
import {GameState, MyLocationGameService} from ".";
import { Player } from "@shared/player_pb";
import { Score } from "@shared/score_pb";
import { Disposable } from "reactor-core-js";
import * as $ from 'jquery';
import SceneSupport from "../Commons/SceneSupport";


import { Location } from "@shared/location_pb";
import { Point } from "@shared/point_pb";

export default class LeaderboardManager implements SceneSupport {

    private playerServiceDisposable: Disposable;
    private leaderboard: Map<String, Score.AsObject>
    private overlay: JQuery<HTMLElement>
    constructor(
        private scene: Phaser.Scene, private state: GameState, private config: GameConfig,
        private playerService: PlayerService
    ) {
        this.leaderboard = new Map();
        this.overlay = $("#phaser-overlay");
        this.playerServiceDisposable = playerService.players()
            .consume(player => this.doOnPlayerScore(player));
    }

    doOnPlayerScore(player: Player.AsObject): void {
        console.log('player score', player);
        this.leaderboard.set(player.uuid, {
            score: player.score,
            username: player.nickname || 'Incognito',
            uuid: player.uuid
        });

    }

    update() {
        var overlay = this.overlay;
        var leaderboard = overlay.find(".leaderboard").find('ol');
        leaderboard.empty();
        const lines = [];
        for (let key of this.leaderboard.keys()) {
            let data = this.leaderboard.get(key);
            lines.push(data);
        }
        lines.sort((a, b) => a.score < b.score ? 1 : -1);
        lines.forEach(line => {
            var elem = $("<li></li>");
            elem.text(line.username + " - " + line.score);
            elem.appendTo(leaderboard);
        })
    }

    dispose(): void {
        this.playerServiceDisposable.dispose();
    }

}