import { Location } from "@shared/location_pb";
import { Flux } from "reactor-core-js/flux";

export default class MyLocationGameService {

    constructor(private locationStream: Flux<Location.AsObject>) { }

    playerLocation(): Flux<Location.AsObject> {
        return this.locationStream;
    }
}