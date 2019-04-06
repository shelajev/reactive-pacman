import {DirectProcessor, Flux} from 'reactor-core-js/flux';
import {Extra} from 'game-idl';
import ExtrasService from '../ExtrasService';
import {ExtrasRepository, PlayerRepository} from '../../repository';

export default class DefaultExtrasService implements ExtrasService {
    extrasProcessor: DirectProcessor<Extra> = new DirectProcessor();

    powerUpTimer: NodeJS.Timeout;
    powerUpActive: boolean;

    constructor(
        private extrasRepository: ExtrasRepository,
        private playerRepository: PlayerRepository
    ) {
        extrasRepository.saveAll(DefaultExtrasService.generate(60, 60, 11))
    }


    extras(): Flux<Extra> {
        return this.extrasProcessor;
    }

    check(x: number, y: number): number {
        const retainedExtra = this.extrasRepository.collideExtra(x, y);
        
        if (retainedExtra != 0) {
            if (Math.sign(retainedExtra) < 0) {
                if (this.powerUpActive)
                    clearTimeout(this.powerUpTimer);
                
                
                this.powerUpTimer = setTimeout(this.setPowerup.bind(this, false), 10000);
                this.powerUpActive = true;
            }
            const addedExtra = this.extrasRepository
                .createExtra(this.playerRepository.findAll().length);

            const extra = new Extra();
            extra.setLast(retainedExtra);
            extra.setCurrent(addedExtra); 
            this.extrasProcessor.onNext(extra);

            return retainedExtra;
        }

        return 0;
    }

    public isPowerupActive(): boolean {
        return this.powerUpActive;
    }

    private setPowerup(value: boolean): void {
        this.powerUpActive = value;
    }

    static generate(width: number, height: number, offset: number): number[] {
        console.log('generating food');
        const iterations =
            ((width - 2 * offset) * (height - 2 * offset) * (0.3 + Math.random() * 0.3));
        const extras = [];
        console.log('iterations', iterations);
        for (let i = 0; i < iterations; i++) {
            extras[i] = DefaultExtrasService.randomPosition(width, height, offset);
        }
        console.log('generated', extras);
        return extras;
    }

    static randomPosition(width: number, height: number, offset: number): number {
        return Math.round(Math.floor(Math.random() * (width - 2 * offset + 1) + offset) +  Math.floor(Math.random() * (height - 2 * offset + 1) + offset) * height);
    }
}
