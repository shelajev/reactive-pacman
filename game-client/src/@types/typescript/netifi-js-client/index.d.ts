

declare module 'netifi-js-client' {
    import {DuplexConnection, ReactiveSocket, Responder} from 'rsocket-types';
    import {Single} from 'rsocket-flowable';
    import {PayloadSerializers} from 'rsocket-core';
    import {RequestHandlingRSocket, RpcClient} from 'rsocket-rpc-core';

    export type NetifiConfig = {
        serializers?: PayloadSerializers<Buffer, Buffer>,
        setup: {
            group: string,
            destination?: string,
            tags?: Tags,
            keepAlive?: number,
            lifetime?: number,
            accessKey: number,
            accessToken: string,
            connectionId?: string,
            additionalFlags?: {
                public?: boolean,
            },
        },
        transport: {
            url?: string,
            wsCreator?: (url: string) => WebSocket,
            // encoder?: Encoders<*>, *** Right now only BufferEncoder is supported for WebSocket so do not allow passing it in if using a URL ***
            connection?: DuplexConnection,
        },
        responder?: Responder<Buffer, Buffer>,
    };

    export class Netifi {
        _client: RpcClient<Buffer, Buffer>;
        _group: string;
        _tags: Tags;
        _connect: () => Single<ReactiveSocket<Buffer, Buffer>>;
        _connecting: Object;
        _connection: ReactiveSocket<Buffer, Buffer>;
        _requestHandler: RequestHandlingRSocket;
        _lastConnectionAttemptTs: number;
        _attempts: number;

        constructor(
            group: string,
            tags: Tags,
            netifiClient: RpcClient<Buffer, Buffer>,
            requestHandler: RequestHandlingRSocket,
        );

        myGroup(): string

        myTags(): Tags

        broadcast(group: string, tags?: Tags): ReactiveSocket<Buffer, Buffer>

        group(group: string, tags?: Tags): ReactiveSocket<Buffer, Buffer>

        destination(
            destination: string,
            group: string,
        ): ReactiveSocket<Buffer, Buffer>

        addService(service: string, handler: Responder<Buffer, Buffer>): void

        close(): void

        static create(config: NetifiConfig): Netifi
    }

    export type Tags = {
        [key: string]: string,
    };
}