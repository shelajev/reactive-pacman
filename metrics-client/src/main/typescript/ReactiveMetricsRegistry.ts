import { SimpleMeterRegistry, IMeter } from "rsocket-rpc-metrics";
import { MetricsSnapshot } from "metrics-idl"
import { ISubscription, ISubscriber } from "rsocket-types";
import { Flowable } from "rsocket-flowable";
import { convert } from "./MappingUtils";


export default class ReactiveMetricsRegistry extends SimpleMeterRegistry {

    private requestedFromDownstream: number;
    private snapshotSubscriber: ISubscriber<MetricsSnapshot>;

    constructor() {
        super();

        setInterval(() => {
            const sink = this.snapshotSubscriber;
            const requested = this.requestedFromDownstream;

            if (sink && requested > 0) {
                sink.onNext(
                    this.meters()
                        .reduce(
                            (ms: MetricsSnapshot, meter: IMeter) => {
                                convert(meter).forEach(m => ms.addMeters(m));
                                return ms;
                            },
                            new MetricsSnapshot(),
                        )
                );
                this.requestedFromDownstream--;
            }
        }, 300)
    }

    asFlowable(): Flowable<MetricsSnapshot> {
        return new Flowable((s: ISubscriber<MetricsSnapshot>) => {
            if (this.snapshotSubscriber) {
                s.onSubscribe({
                    cancel: () => { },
                    request: (n) => { }
                });
                s.onError(new Error("Allowed only a single subscriber"));
                return;
            }

            this.requestedFromDownstream = 0;
            this.snapshotSubscriber = s;

            const subscription: ISubscription = {
                cancel: () => {
                    this.snapshotSubscriber = undefined;
                },
                request: (n: number) => {
                    this.requestedFromDownstream += n;
                }
            };

            this.snapshotSubscriber.onSubscribe(subscription);
        })
    }
}
