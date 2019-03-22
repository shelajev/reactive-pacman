import GameService from "../GameService";
import { Nickname } from "game-idl";
import { Config } from "game-idl";
import { ReactiveSocket } from "rsocket-types";
import { RSocketRPCServices } from "game-idl";
import { Single } from "rsocket-flowable";
import {IMeterRegistry} from "rsocket-rpc-metrics";

export default class GameServiceClientAdapter implements GameService {

    private service: RSocketRPCServices.GameService;

    constructor(rSocket: ReactiveSocket<any, any>, meterRegistry: IMeterRegistry) {
        this.service = new RSocketRPCServices.GameServiceClient(rSocket, undefined, meterRegistry);
    }

    start({ value }: Nickname.AsObject): Single<Config.AsObject> {
        const nicknameProto = new Nickname();

        nicknameProto.setValue(value);

        return this.service.start(nicknameProto)
            .map((c: Config) => c.toObject());
    }
}