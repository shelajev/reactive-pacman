import { Express } from 'express';
import { SSE } from 'express-sse';
import { gameService } from 'lib/services';
import { Nickname } from '@shared/player_pb';


export default (app: Express) => {
    app.post('/start', (req: Express.Request, res: Express.Response) => {
        const sse = new SSE();
        sse.init(req, res);
        const nickname = req.body.nickname as Nickname;
        gameService.start(nickname, req.cookies.decode('uuid'))
            .then(sse.send);
    });
}
