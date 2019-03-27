
import { Express, Request, Response } from 'express';
import { SSE } from 'express-sse';
import { ExtrasService } from '../../service';


export default (app: Express, extrasService: ExtrasService) => {
    app.get('/extras', (req: Request, res: Response) => {
        const sse = new SSE();
        sse.init(req, res);
        extrasService.extras()
            .doOnNext(sse.send)
            .consume();
    });
}
