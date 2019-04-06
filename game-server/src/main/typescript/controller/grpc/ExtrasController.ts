import { ExtrasService } from "../../service";
import { GRPCServices, Extra } from "game-idl";
import { ServerWriteableStream } from "grpc";

export class GrpcExtrasController implements GRPCServices.IExtrasServiceServer {
    constructor(private extrasService: ExtrasService) {}
    
    extras(call: ServerWriteableStream<Extra>) {
        this.extrasService.extras()
            .consume((extra) => call.write(extra), e => call.end(), () => call.end());
    }

}
