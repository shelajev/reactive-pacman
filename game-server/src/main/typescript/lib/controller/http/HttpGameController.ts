import { Express, Request, Response } from 'express';
import { SSE } from 'express-sse';
import { GameService } from '../../service/';
import { Nickname } from '@shared/player_pb';


export default (app: Express, gameService: GameService) => {
    app.post('/start', (req: Request, res: Response) => {
        const sse = new SSE();
        sse.init(req, res);
        const nickname = req.body.nickname as Nickname;
        return res.send(gameService.start(nickname));
    });
}
