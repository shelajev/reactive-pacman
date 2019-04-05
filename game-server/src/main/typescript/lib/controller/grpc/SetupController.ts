import { MapService } from "../../service";

export class GrpcSetupController {
    constructor(private mapService: MapService) {}
    async get(call: any, cb: any) {
        console.log('setup call', call.request);
        cb(null, this.mapService.getMap());
    }
}
