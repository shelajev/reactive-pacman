import { Flux } from 'reactor-core-js/flux';
import { Extra } from '@shared/extra_pb';

export interface ExtrasService {

    extras(): Flux<Extra>;

    check(x: number, y: number): number;

    isPowerupActive(): boolean;
}
