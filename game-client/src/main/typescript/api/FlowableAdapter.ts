import { Publisher, Subscriber, Subscription } from "reactor-core-js/reactive-streams-spec";

export default class FlowableAdapter<T> implements Publisher<T> {

    static MAX_REQUEST_N = 0x7fffffff; // uint31

    static wrap<T>(source: Publisher<T>): FlowableAdapter<T> {
        return new FlowableAdapter<T>(source);
    }

    constructor(private source: Publisher<T>) {}

    subscribe<S extends T>(s: Subscriber<S>): void {
        this.source.subscribe({
            onSubscribe: (subscription: Subscription) => {
                s.onSubscribe({
                    request: (n) => subscription.request(n > FlowableAdapter.MAX_REQUEST_N ? FlowableAdapter.MAX_REQUEST_N : n),
                    cancel: () => subscription.cancel()
                })
            },
            onNext: (t: S) => s.onNext(t),
            onError: (e: Error) => s.onError(e),
            onComplete: () => s.onComplete(),
        })
    }
    
}