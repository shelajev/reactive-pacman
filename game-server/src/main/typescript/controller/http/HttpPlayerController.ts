import {Express, Request, Response} from 'express';
import {PlayerService} from '../../service';
import {Location} from 'game-idl';
import * as SSE from 'express-sse';

export default (app: Express, playerService: PlayerService) => {

    app.post('/http/locate', (req: Request, res: Response) => {
        const location = req.body.location as Location;
        const uuid = req.cookies.decode("uuid");

        playerService.locate(uuid, location);
    });

    app.get('/http/players', (req: Request, res: Response) => {
        const sse = new SSE.default();
        sse.init(req, res);

        playerService.players()
            .doOnNext(sse.send)
            .consume();
    });
}
