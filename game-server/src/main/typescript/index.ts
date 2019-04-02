import 'module-alias/register'
import * as express from 'express';
import * as cors from 'cors';
import * as bodyParser from 'body-parser';

import httpAPI from './lib/controller/http';
import { SetupController as rsocketAPI} from './lib/controller/rsocket/SetupController';
import { 
    DefaultPlayerService,
    DefaultExtrasService,
    DefaultGameService,
    DefaultMapService
} from './lib/service'
import { InMemoryPlayerRepository, InMemoryExtrasRepository } from './lib/repository';
import { RSocketServer } from 'rsocket-core';


const app = express();

const options: cors.CorsOptions = {
    allowedHeaders: ["Origin", "X-Requested-With", "Content-Type", "Accept", "X-Access-Token"],
    credentials: true,
    methods: "GET,HEAD,OPTIONS,PUT,PATCH,POST,DELETE",
    origin: ['http://localhost:9000', 'ws://localhost:9000'],
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

const rsocket = new rsocketAPI(mapService, extrasService, playerService, gameService, playerRepository, app);
const rsocketServer = new RSocketServer({
    getRequestHandler: rsocket.handler.bind(rsocket),
    transport: rsocket.transport()
});
rsocket.server.listen(3000);
rsocketServer.start();

