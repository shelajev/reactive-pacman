import * as Game from "../Game";
import Model from "./Model";

export default interface Config extends Game.Config {
    readonly model: Model;
}