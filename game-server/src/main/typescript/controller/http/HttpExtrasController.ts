import { Express, Request, Response } from 'express';
import * as SSE from 'express-sse';
import { ExtrasService } from '../../service';


export default (app: Express, extrasService: ExtrasService) => {
    app.get('http/extras', (req: Request, res: Response) => {
        const sse = new SSE.default();
        sse.init(req, res);
        extrasService.extras()
            .doOnNext(sse.send)
            .consume();
    });
}
