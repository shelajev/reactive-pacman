import { PlayerService } from "../../service";

export class GrpcPlayerController {
    constructor(private playerService: PlayerService) {}
    async players(call: any, cb: any) {
        console.log('player call', call.request);
        this.playerService.players()
            .doOnNext(call.write);
    }

}
