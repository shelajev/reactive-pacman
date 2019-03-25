declare module 'express-sse' {
  import { Express } from 'express';
  import * as stream from "stream";
  import * as http from "http";
  export class SSE<T> {
    constructor(events?: T);
    public init(req: Express.Request, res: Express.Response): void;
    public send(event: any): void;
  }
}
