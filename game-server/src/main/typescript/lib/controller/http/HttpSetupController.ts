import { Express, Response, Request } from 'express';
import { v4 }from 'uuid';
import { MapService } from '../../service/';

export default (app: Express, mapService: MapService) => {
  app.get('/setup', (req: Request, res: Response) => {
    const uuid = v4();
    res.cookie('uuid', uuid);
    res.send(200, mapService.getMap());
  });
}
