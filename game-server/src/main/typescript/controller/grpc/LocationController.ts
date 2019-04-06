import {PlayerService} from "../../service";
import {Empty} from "google-protobuf/google/protobuf/empty_pb";
import {GRPCServices, Location} from 'game-idl'
import {sendUnaryData, ServerUnaryCall} from "grpc";

export class GrpcLocationController implements GRPCServices.ILocationServiceServer {
    constructor(private playerService: PlayerService) {
    }
    locate(call: ServerUnaryCall<Location>, callback: sendUnaryData<Empty>): void {
        this.playerService.locate(call.metadata.get('uuid')[0] as string, call.request);
        callback(null, new Empty());
    }

}
