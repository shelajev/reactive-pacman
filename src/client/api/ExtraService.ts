import { Flux } from "reactor-core-js/flux";
import { Extra } from "@shared/extra_pb";

export default interface ExtraService {
    
    food(): Flux<Extra.AsObject>;
    power(): Flux<Extra.AsObject>;
}