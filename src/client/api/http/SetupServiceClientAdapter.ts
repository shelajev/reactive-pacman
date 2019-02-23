import { Single } from "rsocket-flowable";
import { Map } from "@shared/map_pb";

export default class SetupServiceClientAdapter {

    map(): Single<Map.AsObject> {
        return new Single(subject => {
            subject.onSubscribe();
            fetch("http://localhost:3000/http/setup", {
                credentials: "include"
            })
            .then(response => response.arrayBuffer())
            .then(buffer => subject.onComplete(Map.deserializeBinary(new Uint8Array(buffer)).toObject()), error => subject.onError(error))
        })
    }
}