import { ReactiveSocket } from "rsocket-types";
import { ExtraServiceClient } from "@shared/service_rsocket_pb";
import ExtraService from "../ExtraService";
import { Extra } from "@shared/extra_pb";
import { Flux } from "reactor-core-js/flux";
import { Empty } from "google-protobuf/google/protobuf/empty_pb";

export default class ExtraServiceClientAdapter implements ExtraService {

    private service: any;

    constructor(rSocket: ReactiveSocket<any, any>) {
        this.service = new ExtraServiceClient(rSocket);
    }

    food(): Flux<Extra.AsObject> {
        return Flux.from<Extra>(this.service.food(new Empty()))
            .map(extra => extra.toObject());
    }

    power(): Flux<Extra.AsObject> {
        return Flux.from<Extra>(this.service.power(new Empty()))
            .map(extra => extra.toObject());
    }
}