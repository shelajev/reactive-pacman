import { Request } from 'express';
import * as cookieParser from 'cookie-parser';

declare module 'express' {

  interface Request {
      body: any;
      cookies: cookieParser.CookieParseOptions
      uuid: string;
  }   
  
  interface Response {
    send: Send
  }

  interface Send {
    (status: number, body?: any): Response;
    (body: any): Response;
  }

}