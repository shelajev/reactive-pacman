import { Player } from "@shared/player_pb";
import { Tile } from "@shared/tile_pb";

export default class GameState {
    tiles: Tile.AsObject[];

    powerState: number;
    player: Player.AsObject;
    players: { [key: string]: Player.AsObject };
}