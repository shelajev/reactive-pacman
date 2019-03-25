
import { Express } from 'express';
import { SSE } from 'express-sse';
import { extrasService } from 'lib/services';


export default (app: Express) => {
    app.get('/extras', (req: Express.Request, res: Express.Response) => {
        const sse = new SSE();
        sse.init(req, res);
        extrasService.extras()
            .doOnNext(sse.send)
            .subscribe();
    });
}
