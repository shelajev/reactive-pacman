import GameService from "../GameService";
import { Nickname } from "game-idl";
import { Config } from "game-idl";
import { Single } from "rsocket-flowable";

export default class GameServiceClientAdapter implements GameService {

    start({ value }: Nickname.AsObject): Single<Config.AsObject> {
        const nicknameProto = new Nickname();

        nicknameProto.setValue(value);

        return new Single(subject => {
            subject.onSubscribe();
            fetch("http://localhost:3000/http/start", {
                method: "POST",
                body: nicknameProto.serializeBinary(),
                credentials: "include"
            })
            .then(response => response.arrayBuffer())
            .then(buffer => subject.onComplete(Config.deserializeBinary(new Uint8Array(buffer)).toObject()), error => subject.onError(error))
        })
    }
}