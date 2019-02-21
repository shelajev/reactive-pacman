import { ExtrasServiceServer } from '@shared/service_rsocket_pb';
import { extrasProcessor } from '../processors';


const extrasService = new ExtrasServiceServer({
    extras() {
        return extrasProcessor;
    }
});

export default extrasService;
