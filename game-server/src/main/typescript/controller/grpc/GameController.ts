import { GameService } from "../../service";
import { ServerUnaryCall, sendUnaryData } from "grpc";
import { Nickname, Config } from "game-idl";
import { GRPCServices } from "game-idl";

export class GrpcGameController implements GRPCServices.IGameServiceServer {
    constructor(private gameService: GameService) {}
    start(call: ServerUnaryCall<Nickname>, callback: sendUnaryData<Config>) {
        console.log('game call', call.request);
        callback(null, this.gameService.start(call.metadata.get('uuid')[0] as string, call.request));
    }

}
