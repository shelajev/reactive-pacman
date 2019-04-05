import { GameService } from "../../service";

export class GrpcGameController {
    constructor(private gameService: GameService) {}
    async start(call: any, cb: any) {
        console.log('game call', call.reqeust);
        cb(null, this.gameService.start('aaa', call.request));
    }

}
