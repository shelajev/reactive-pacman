import RSocketWebSocketClient from 'rsocket-websocket-client';
import {RSocketClient, Utf8Encoders} from 'rsocket-core';
import {Responder, Payload} from 'rsocket-types';
import {Single} from 'rsocket-flowable';

(() => {
    const client = new RSocketClient({
        transport: new RSocketWebSocketClient(
            {
                url: 'ws://localhost:8080',
            },
            Utf8Encoders,
        ),
        setup: {
            dataMimeType:"text/plain",
            metadataMimeType:"text/plain",
            keepAlive: 5000,
            lifetime: 60000,
        },
        responder: {
            requestResponse: payload => {
                console.log("Got payload", payload);
                setInterval(() => console.log("₿ Mining ₿"), 10);
                return Single.of({
                    data: "Yes. I'm going to mine some ₿",
                    metadata: ""
                })
            }
        }
    });

    client.connect()
        .then(rsocket => {
            console.log("Connected");
            rsocket
                .requestStream({
                    data: "Clients Request",
                    metadata: ""
                })
                .subscribe({
                    onSubscribe: s => s.request(10),
                    onNext: (p) => console.log("Got response", p),
                    onError: (e) => console.error(e),
                    onComplete: () => console.log("Done")
                });
        });
})();