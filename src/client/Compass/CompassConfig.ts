import * as Game from "../Game";
import Service from "./CompassService";

export default interface CompassConfig extends Game.Config {
    readonly service: Service;
}