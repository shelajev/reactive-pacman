import ExtrasService from "../ExtrasService";
import { Extra } from "@shared/extra_pb";
import { Flux } from "reactor-core-js/flux";
import { Empty } from "google-protobuf/google/protobuf/empty_pb";
import FlowableAdapter from "../FlowableAdapter";
import { ExtrasServiceClient } from "@shared/service_grpc_web_pb";
import { Flowable } from "rsocket-flowable";

export default class ExtrasServiceClientAdapter implements ExtrasService {

    private service: ExtrasServiceClient;

    constructor() {
        this.service = new ExtrasServiceClient("http://localhost:8000", {}, {});
    }

    extras(): Flux<Extra.AsObject> {
        return Flux.from<Extra>(FlowableAdapter.wrap(new Flowable(subscriber => {
                const clientReadableStream = this.service.extras(new Empty(), { "uuid": localStorage.getItem("uuid") });

                subscriber.onSubscribe({
                    request: (): void => { },
                    cancel: (): void => clientReadableStream.cancel()
                });

                clientReadableStream.on("data", response => subscriber.onNext(response));
                clientReadableStream.on("end", () => subscriber.onComplete());
                clientReadableStream.on("error", (err) => subscriber.onError(new Error(`An Grpc Error was thrown. Code: [${err.code}]. Message: ${err.message}`)));
            })))
            .map(player => player.toObject())
    }
}