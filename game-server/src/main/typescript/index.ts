import * as express from 'express';
import * as cors from 'cors';
import * as bodyParser from 'body-parser';
import * as grpc from 'grpc';
import {
    GRPCServices
} from 'game-idl';
import httpAPI from './controller/http';
import { SetupController as rsocketAPI} from './controller/rsocket/SetupController';
import {
    DefaultPlayerService,
    DefaultExtrasService,
    DefaultGameService,
    DefaultMapService
} from './service'
import { InMemoryPlayerRepository, InMemoryExtrasRepository } from './repository';
import { RSocketServer } from 'rsocket-core';
import { GrpcSetupController } from './controller/grpc/SetupController';
import { GrpcGameController } from './controller/grpc/GameController';
import { GrpcExtrasController } from './controller/grpc/ExtrasController';
import { GrpcLocationController } from './controller/grpc/LocationController';
import { GrpcPlayerController } from './controller/grpc/PlayerController';
import * as io from 'socket.io';
import { Server } from "http";
import {socketIOSetup} from './controller/socket.io/socketio';

const app = express();
const httpServer = new Server();

const options: cors.CorsOptions = {
    allowedHeaders: ["Origin", "X-Requested-With", "Content-Type", "Accept", "X-Access-Token"],
    credentials: true,
    methods: "GET,HEAD,OPTIONS,PUT,PATCH,POST,DELETE",
    origin: ['*'],
    preflightContinue: false
  };

app.use(bodyParser.json());

app.use(cors(options));

const playerRepository = new InMemoryPlayerRepository();
const extrasRepository = new InMemoryExtrasRepository();
const mapService = new DefaultMapService();
const extrasService = new DefaultExtrasService(extrasRepository, playerRepository);
const playerService = new DefaultPlayerService(playerRepository, extrasService, mapService)
const gameService = new DefaultGameService(playerService, extrasRepository, playerRepository);

const rsocket = new rsocketAPI(mapService, extrasService, playerService, gameService, httpServer);
const rsocketServer = new RSocketServer({
    getRequestHandler: rsocket.handler.bind(rsocket),
    transport: rsocket.transport()
});

const sIOServer = new Server();
const socketIOServer = io(sIOServer, {
    transports: ["websocket"]
});
sIOServer.listen(5900);

socketIOSetup(mapService, extrasService, playerService, gameService, socketIOServer);

httpServer.listen(3000);
rsocketServer.start();

const server = new grpc.Server();

server.addService(GRPCServices.SetupServiceService, new GrpcSetupController(mapService));
server.addService(GRPCServices.GameServiceService, new GrpcGameController(gameService));
server.addService(GRPCServices.ExtrasServiceService, new GrpcExtrasController(extrasService));
server.addService(GRPCServices.LocationServiceService, new GrpcLocationController(playerService));
server.addService(GRPCServices.PlayerServiceService, new GrpcPlayerController(playerService));
server.bind('127.0.0.1:9090', grpc.ServerCredentials.createInsecure());
server.start();
console.log('grpc started');
