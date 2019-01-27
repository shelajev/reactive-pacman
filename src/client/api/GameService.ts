import { Config } from "@shared/config_pb";
import { Nickname } from "@shared/player_pb";
import { Single } from "rsocket-flowable";


export default interface GameService {

    start(nickname: Nickname.AsObject): Single<Config.AsObject>
}