import { Express, Response, Request } from 'express';
import { PlayerService } from '../../service/';
import { DirectProcessor } from 'reactor-core-js/flux';
import { Location } from '@shared/location_pb';
import { SSE } from 'express-sse';

export default (app: Express, playerService: PlayerService) => {
    const locationDirectionProcessors: Map<string, DirectProcessor<Location>> = new Map();

    app.post('locate', (req: Request, res: Response) => {
        const location = req.body.location as Location;
        const uuid = req.uuid;
        let processor = locationDirectionProcessors.get(uuid);
        if (!processor) {
            processor = new DirectProcessor<Location>();
            playerService.locate(uuid, processor);
            locationDirectionProcessors.set(uuid, processor);
        }

        processor.onNext(location);
        
    });

    app.get('/players', (req: Request, res: Response) => {
        const sse = new SSE();
        sse.init(req, res);

        playerService.players()
            .doOnNext(sse.send)
            .consume();
    });
}
