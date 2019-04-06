import { PlayerService } from "../../service";
import { ServerUnaryCall, sendUnaryData, ServerWriteableStream, ServerReadableStream } from "grpc";
import { Player } from "game-idl";
import { GRPCServices } from "game-idl";
import { Empty } from "google-protobuf/google/protobuf/empty_pb";

export class GrpcPlayerController implements GRPCServices.IPlayerServiceServer {
    constructor(private playerService: PlayerService) {}

    locate(call: ServerReadableStream<Location>, callback: sendUnaryData<Empty>): void {
        
    }

    players(call: ServerWriteableStream<Player>): void {
        this.playerService.players()
            .consume(player => call.write(player), t => call.end(), () => call.end());
    }

}
