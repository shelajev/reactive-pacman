import {Server} from 'socket.io';
import {ExtrasService, GameService, MapService, PlayerService} from "../../service";
import {Location, Nickname} from "game-idl";

export const socketIOSetup = (
    mapService: MapService,
    extrasService: ExtrasService,
    playerService: PlayerService,
    gameService: GameService,
    server: Server) => {

    playerService
        .players()
        .consume(player => server.sockets.emit("players", new Buffer(player.serializeBinary())));


    extrasService
        .extras()
        .consume(extra => server.sockets.emit("extras", new Buffer(extra.serializeBinary())));

    server.on("connection", (socket) => {
        socket.on("disconnect", () => {
            playerService.disconnectPlayer(socket.id);
        });

        socket.on("start", (data: ArrayBuffer, ack: Function) => {
            const nickname = Nickname.deserializeBinary(new Uint8Array(data));

            ack(new Buffer(gameService.start(socket.id, nickname).serializeBinary()));
        });

        socket.on("locate", (data: ArrayBuffer) => {
            const location = Location.deserializeBinary(new Uint8Array(data));

            playerService.locate(socket.id,location);
        });

        socket.emit("setup", new Buffer(mapService.getMap().serializeBinary()));
    });
};