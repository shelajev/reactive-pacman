import { Express } from 'express';
import { SSE } from 'express-sse';
import { gameService, playerService } from 'lib/services';
import { Nickname } from '@shared/player_pb';
import { DirectProcessor } from 'reactor-core-js/flux';


export default (app: Express) => {
    app.post('locate', (req: Express.Request, res: Express.Response) => {
        const location = req.body.location as Location;
        const uuid = req.uuid;
        playerService.locate(new DirectProcessor(), uuid);
        location
    });
}

//         UnicastProcessor<Location> processor = locationDirectProcessors.computeIfAbsent(uuid, __ -> {
//             UnicastProcessor<Location> unicastProcessor =
//                 UnicastProcessor.create(
//                     Queues.<Location>unboundedMultiproducer().get(),
//                     () -> locationDirectProcessors.remove(uuid)
//                 );

//             playerService.locate(unicastProcessor)
//                          .subscriberContext(Context.of("uuid", uuid))
//                          .subscribe();

//             return unicastProcessor;
//         });

//         processor.onNext(location);
//     }

//     @GetMapping("/players")
//     @CrossOrigin(origins = "*", methods = RequestMethod.GET, allowedHeaders = "*", allowCredentials = "true")
//     public Flux<String> players(@CookieValue("uuid") String uuid) {
//         return playerService.players()
//                             .map(e -> Arrays.toString(e.toByteArray()))
//                             .onBackpressureDrop()
//                             .transform(Metrics.<String>timed(registry, "http.server", "service", org.coinen.pacman.PlayerService.SERVICE, "method", org.coinen.pacman.PlayerService.METHOD_PLAYERS))
//                             .subscriberContext(Context.of("uuid", UUID.fromString(uuid)));
//     }
// }
