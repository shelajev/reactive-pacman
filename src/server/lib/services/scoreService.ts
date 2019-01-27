import { ExtraServiceServer } from '@shared/service_rsocket_pb';
import { Player } from '@shared/player_pb';
import scoreProcessor from '../processors/scoreProcessor';

const ScoreService = new ExtraServiceServer({
    score(player: Player) {
        return scoreProcessor;
    }

});

export default ScoreService;
