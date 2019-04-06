import * as rsocket_flowable from 'rsocket-flowable';
import * as google_protobuf_empty_pb from 'google-protobuf/google/protobuf/empty_pb';
import {Extra, RSocketRPCServices} from "game-idl";
import {ExtrasService} from '../../service';

export class ExtrasController implements RSocketRPCServices.ExtrasService {
    constructor(private extrasService: ExtrasService) {}

    extras(message: google_protobuf_empty_pb.Empty, metadata?: Buffer): rsocket_flowable.Flowable<Extra> {
        return this.extrasService.extras() as any;
    }

}
