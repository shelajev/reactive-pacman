import { ExtrasService } from "../../service";

export class GrpcExtrasController {
    constructor(private extrasService: ExtrasService) {}
    async extras(call: any, cb: any) {
        console.log('extras call', call.reqeust);
        this.extrasService.extras()
            .doOnNext(call.write);
    }

}
