import GameService from "../GameService";
import { Nickname } from "@shared/player_pb";
import { Config } from "@shared/config_pb";
import { Single } from "rsocket-flowable";
import { GameServiceClient } from "@shared/service_grpc_web_pb";
import { ClientReadableStream } from "grpc-web";

export default class GameServiceClientAdapter implements GameService {

    private service: GameServiceClient;

    constructor() {
        this.service = new GameServiceClient("http://localhost:8000", {}, {});
    }

    start({ value }: Nickname.AsObject): Single<Config.AsObject> {
        const nicknameProto = new Nickname();

        nicknameProto.setValue(value);

        return new Single(subject => {
            let stream: ClientReadableStream<any>;
            subject.onSubscribe();
            stream = this.service.start(nicknameProto as any, { "uuid": localStorage.getItem("uuid") }, (err, response) => {
                if (err) {
                    subject.onError(new Error(`An Grpc Error was thrown. Code: [${err.code}]. Message: ${err.message}`));
                    return;
                }
                subject.onComplete((response.toObject() as any) as Config.AsObject);
            });
        })
    }
}