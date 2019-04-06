import {MapService} from "../../service";
import {GRPCServices, Map} from "game-idl";
import {sendUnaryData, ServerUnaryCall} from "grpc";
import {Empty} from "google-protobuf/google/protobuf/empty_pb";

export class GrpcSetupController implements GRPCServices.ISetupServiceServer {
    constructor(private mapService: MapService) {}
    
    get(call: ServerUnaryCall<Empty>, callback: sendUnaryData<Map>) {
        callback(null, this.mapService.getMap());
    }
}
