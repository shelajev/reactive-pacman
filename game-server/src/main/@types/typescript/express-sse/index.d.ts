declare module 'express-sse' {
  import { Request, Response } from 'express';
  import * as stream from "stream";
  import * as http from "http";
  export default class SSE {
    constructor(events?: any);
    public init(req: Request, res: Response): void;
    public send(event: any): void;
  }
}
