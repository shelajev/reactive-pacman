import {PlayerService} from "../../service";
import {Empty} from "google-protobuf/google/protobuf/empty_pb";
import {Flux} from "reactor-core-js/flux";
import {GRPCServices, Location} from 'game-idl'
import { ServerUnaryCall, sendUnaryData } from "grpc";

export class GrpcLocationController implements GRPCServices.ILocationServiceServer {
    constructor(private playerService: PlayerService) {
    }
    locate(call: ServerUnaryCall<Location>, callback: sendUnaryData<Empty>): void {
        console.log('got loc', call.request);
        this.playerService.locate(call.metadata.get('uuid')[0] as string, Flux.just(call.request))
            .consume();
        callback(null, new Empty());
    }

}
