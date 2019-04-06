import { RSocketRPCServices } from "game-idl";
import { ExtrasService, GameService, MapService, PlayerService } from '../../service';
import { BufferEncoders } from 'rsocket-core'
import RSocketWebSocketServer from 'rsocket-websocket-server'
import { RequestHandlingRSocket } from 'rsocket-rpc-core'
import { v4 } from 'uuid';
import { ConnectionStatus, ReactiveSocket } from 'rsocket-types';
import { GameController } from './GameController';
import { PlayerController } from './PlayerController';
import { ExtrasController } from './ExtrasController';

import { Server } from 'http';

export class SetupController {

  constructor(
    private mapService: MapService,
    private extrasService: ExtrasService,
    private playerService: PlayerService,
    private gameService: GameService,
    private server: Server
  ) { }

  handler(socket: ReactiveSocket<any, any>) {
    const uuid = v4();
    socket.connectionStatus()
      .subscribe((cs: ConnectionStatus) => {
        if (cs.kind == 'CLOSED' || cs.kind == 'ERROR') {
          this.playerService.disconnectPlayer(uuid);
        }
      });
    const map = this.mapService.getMap();
    new RSocketRPCServices.MapServiceClient(socket).setup(map);
    const handler = new RequestHandlingRSocket();

    handler.addService('org.coinen.pacman.GameService', new RSocketRPCServices.GameServiceServer(new GameController(uuid, this.gameService)));
    handler.addService('org.coinen.pacman.PlayerService', new RSocketRPCServices.PlayerServiceServer(new PlayerController(this.playerService, uuid)));
    handler.addService('org.coinen.pacman.ExtrasService', new RSocketRPCServices.ExtrasServiceServer(new ExtrasController(this.extrasService)));
    return handler;
  }

  transport() {
    return new RSocketWebSocketServer(
      {
        server: this.server
      },
      BufferEncoders
    );
  }
}
