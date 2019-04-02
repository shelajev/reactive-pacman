import * as rsocket_flowable from 'rsocket-flowable';
import * as google_protobuf_empty_pb from 'google-protobuf/google/protobuf/empty_pb';
import { ExtrasService as ExtrasServicePB } from "@shared/service_rsocket_pb";
import { ExtrasService } from '../../service';
import { Extra } from "@shared/extra_pb";

export class ExtrasController implements ExtrasServicePB {
    constructor(private extrasService: ExtrasService) {}

    extras(message: google_protobuf_empty_pb.Empty, metadata?: Buffer): rsocket_flowable.Flowable<Extra> {
        return this.extrasService.extras() as any;
    }

}
