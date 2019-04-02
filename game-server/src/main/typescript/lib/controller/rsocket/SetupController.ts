
import { MapServiceClient, GameServiceServer, PlayerServiceServer, ExtrasServiceServer } from "@shared/service_rsocket_pb";
import { ExtrasService, GameService, PlayerService, MapService } from '../../service';
import { BufferEncoders } from 'rsocket-core'
import RSocketWebSocketServer from 'rsocket-websocket-server'
import { RequestHandlingRSocket } from 'rsocket-rpc-core'
import { v4 } from 'uuid';
import { ReactiveSocket, ConnectionStatus } from 'rsocket-types';
import { GameController } from './GameController';
import { PlayerController } from './PlayerController';
import { ExtrasController } from './ExtrasController';

import { Server } from 'http';

export class SetupController {
  server: Server
    constructor(
      private mapService: MapService,
      private extrasService: ExtrasService,
      private playerService: PlayerService,
      private gameService: GameService,
      app: Express.Application
    ) {
      this.server = new Server(app);
    }

    handler(socket: ReactiveSocket<any, any>) {
      const uuid = v4();
      socket.connectionStatus()
        .subscribe((cs: ConnectionStatus) => {
          if (cs.kind == 'CLOSED' || cs.kind == 'ERROR') {
            this.playerService.disconnectPlayer(uuid);
          }
        });
        try {
        const map = this.mapService.getMap();
        new MapServiceClient(socket).setup(map);
        } catch(e) {
          console.log('err', e.message);
        }
      const handler = new RequestHandlingRSocket();
    
      handler.addService('org.coinen.pacman.GameService', new GameServiceServer(new GameController(uuid, this.gameService)));
      handler.addService('org.coinen.pacman.PlayerService', new PlayerServiceServer(new PlayerController(this.playerService, uuid)));
      handler.addService('org.coinen.pacman.ExtrasService', new ExtrasServiceServer(new ExtrasController(this.extrasService)));
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
