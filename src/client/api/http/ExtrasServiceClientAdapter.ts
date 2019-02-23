import ExtrasService from "../ExtrasService";
import FlowableAdapter from "../FlowableAdapter";
import { Extra } from "@shared/extra_pb";
import { Flux } from "reactor-core-js/flux";
import {Flowable} from "rsocket-flowable";

export default class ExtrasServiceClientAdapter implements ExtrasService {

    extras(): Flux<Extra.AsObject> {
        return Flux.from<Extra>(FlowableAdapter.wrap(new Flowable(subscriber => {
                const eventSource = new EventSource("http://localhost:3000/http/extras", { withCredentials : true });

                subscriber.onSubscribe({
                    request: (): void => {},
                    cancel: (): void => eventSource.close()
                });

                eventSource.onmessage = e => {
                    subscriber.onNext(Extra.deserializeBinary(new Uint8Array(eval(e.data))));
                };

                eventSource.onerror = e => {
                    subscriber.onError(e.data);
                }
            })))
            .map(extra => extra.toObject());
    }
}