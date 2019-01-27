import PlayerService from "../api/PlayerService";
import { Player } from "@shared/player_pb";
import Model from "./Model";
import { Disposable } from "reactor-core-js";

export default class ModelAndView implements Disposable {

    private defaultModel: DefaultModel;
    private disposable: Disposable;

    private closestDistance: number;

    constructor(private me: Player.AsObject, private playerService: PlayerService) {
        this.defaultModel = new DefaultModel();

        this.disposable = playerService.players()
            .consume(player => this.doOnPlayer(player));
    }

    get model(): Model {
        return this.defaultModel;
    }

    dispose(): void {
        this.disposable.dispose();
    }

    private doOnPlayer(player: Player.AsObject): void {
        if (player.type === Player.Type.PACMAN) {    
            const myPosition = this.me.location.position;
            const pacManPosition = player.location.position;

            if (this.closestDistance || )
            this.defaultModel.rotation = 
        }
        
    }

    private distanceTo()
}

class DefaultModel implements Model {
    rotation: number;
}