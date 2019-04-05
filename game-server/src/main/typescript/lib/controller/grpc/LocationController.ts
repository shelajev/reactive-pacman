import { PlayerService } from "../../service";
import { Empty } from "google-protobuf/google/protobuf/empty_pb";
import { Location } from '@shared/location_pb';
import { DirectProcessor, Flux } from "reactor-core-js/flux";

export class GrpcLocationController {
    constructor(private playerService: PlayerService) {
    }
    async locate(call: any, cb: any) {
        this.playerService.locate('aaa', Flux.from(call.request));
        console.log('got loc', call.request);
        cb(null, new Empty());
    }

}
