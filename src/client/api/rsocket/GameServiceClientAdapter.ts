import GameService from "../GameService";
import { Nickname } from "@shared/player_pb";
import { Config } from "@shared/config_pb";
import { ReactiveSocket } from "rsocket-types";
import { GameServiceClient } from "@shared/service_rsocket_pb";
import { Single } from "rsocket-flowable";

export default class GameServiceClientAdapter implements GameService {

    private service: any;

    constructor(rSocket: ReactiveSocket<any, any>) {
        this.service = new GameServiceClient(rSocket);
    }

    start({ value }: Nickname.AsObject): Single<Config.AsObject> {
        const nicknameProto = new Nickname();

        nicknameProto.setValue(value);

        return this.service.start(nicknameProto)
            .map((c: Config) => c.toObject());
    }
}