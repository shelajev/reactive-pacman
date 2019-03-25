import { Request } from 'express';
import * as cookieParser from 'cookie-parser'
declare module 'express' {

  namespace Express {

    interface Request {
        body: any;
        cookies: cookieParser.CookieParseOptions
        uuid: string;
    }   
    
    interface Response {

    }
  }
}