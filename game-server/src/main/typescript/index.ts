import 'module-alias/register'
import * as express from 'express';
import httpAPI from './lib/controller/http';
import { 
    DefaultPlayerService,
    DefaultExtrasService,
    DefaultGameService,
    DefaultMapService
} from './lib/service'
import { InMemoryPlayerRepository, InMemoryExtrasRepository } from './lib/repository';
const app = express();

app.listen(3000, () => {
    const playerRepository = new InMemoryPlayerRepository();
    const extrasRepository = new InMemoryExtrasRepository();
    const mapService = new DefaultMapService();
    const extrasService = new DefaultExtrasService(extrasRepository, playerRepository);
    const playerService = new DefaultPlayerService(playerRepository, extrasService, mapService)
    const gameService = new DefaultGameService(playerService, extrasRepository, playerRepository);

    httpAPI(app, extrasService, gameService, playerService, mapService);
    console.log('Server started');
});
