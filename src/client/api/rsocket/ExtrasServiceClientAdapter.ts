import { ReactiveSocket } from "rsocket-types";
import { ExtrasServiceClient } from "@shared/service_rsocket_pb";
import ExtrasService from "../ExtrasService";
import { Extra } from "@shared/extra_pb";
import { Flux } from "reactor-core-js/flux";
import { Empty } from "google-protobuf/google/protobuf/empty_pb";

export default class ExtrasServiceClientAdapter implements ExtrasService {

    private service: any;

    constructor(rSocket: ReactiveSocket<any, any>) {
        this.service = new ExtrasServiceClient(rSocket);
    }

    extras(): Flux<Extra.AsObject> {
        return Flux.from<Extra>(this.service.extras(new Empty()))
            .map(extra => extra.toObject());
    }
}