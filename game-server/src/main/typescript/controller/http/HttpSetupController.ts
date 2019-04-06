import { Express, Response, Request } from "express";
import { v4 } from "uuid";
import { MapService } from "../../service";

export default (app: Express, mapService: MapService) => {
  app.get("/http/setup", (req: Request, res: Response) => {
    const uuid = v4();
    res.cookie("uuid", uuid);
    const map = mapService.getMap();
    const buff = new Buffer(map.toString()).toString("base64");
    res.send(buff);
  });
};
