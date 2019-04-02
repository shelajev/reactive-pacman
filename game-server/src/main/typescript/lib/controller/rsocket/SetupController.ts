
import * as rsocket_flowable from 'rsocket-flowable';
import * as google_protobuf_empty_pb from 'google-protobuf/google/protobuf/empty_pb';
import { SetupService as SetupServicePB, MapServiceClient, GameServiceServer, PlayerServiceServer, ExtrasServiceServer } from "@shared/service_rsocket_pb";
import FlowableAdapter from './support/FlowableAdapter';
import { ExtrasService, GameService, PlayerService, MapService } from '../../service';
import { Location } from "@shared/location_pb";
import { Map } from '@shared/map_pb';
import { Mono } from 'reactor-core-js/flux';
import { Config } from '@shared/config_pb';
import { Publisher } from 'reactor-core-js/reactive-streams-spec';
import { RSocketServer, BufferEncoders } from 'rsocket-core'
import RSocketWebSocketServer from 'rsocket-websocket-server'
import { RequestHandlingRSocket } from 'rsocket-rpc-core'
import { v4 } from 'uuid';
import { PlayerRepository } from 'lib/repository';
import { Player } from '@shared/player_pb';
import { ReactiveSocket, IPartialSubscriber, ConnectionStatus } from 'rsocket-types';
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
      private playerRepository: PlayerRepository,
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
    
      handler.addService('org.coinen.pacman.GameService', new GameServiceServer(new GameController(this.gameService)));
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
