package org.coinen.reactive.pacman.reactivepacman.service.support;

import org.coinen.pacman.Extra;
import org.coinen.reactive.pacman.reactivepacman.repository.ExtrasRepository;
import org.coinen.reactive.pacman.reactivepacman.repository.PlayerRepository;
import org.coinen.reactive.pacman.reactivepacman.service.ExtrasService;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class DefaultExtrasService implements ExtrasService {
    final ExtrasRepository extrasRepository;
    final PlayerRepository playerRepository;

    final DirectProcessor<Extra> extrasProcessor = DirectProcessor.create();
    final FluxSink<Extra> extrasFluxSink = extrasProcessor.serialize().sink();

    public DefaultExtrasService(ExtrasRepository extrasRepository,
        PlayerRepository playerRepository) {
        this.extrasRepository = extrasRepository;
        this.playerRepository = playerRepository;

        extrasRepository.saveAll(generate(60, 60, 11));
    }

    @Override
    public Flux<Extra> extras() {
        return extrasProcessor;
    }

    @Override
    public int check(float x, float y) {
        var retainedExtra = extrasRepository.collideExtra(x, y);

        if (retainedExtra != 0) {
            if (Math.signum(retainedExtra) == -1.0f) {
                // scoreProcessor.onNext({player, score: player.getScore() + 1});
            }
            else {
                var sec = 10;
//                                   store.setPowerUpEnd(Instant.now() + sec * 1000);
            }
            var addedExtra = extrasRepository.createExtra(playerRepository.findAll()
                                                                          .size());

            var extra = Extra.newBuilder()
                             .setLast(retainedExtra)
                             .setCurrent(addedExtra)
                             .build();

            extrasFluxSink.next(extra);

            return addedExtra;
        }

        return 0;
    }

    static int[] generate(int width, int height, int offset) {
        var iterations =
            (int) ((width - 2 * offset) * (height - 2 * offset) * (0.3 + Math.random() * 0.3));
        var extras = new int[iterations];

        for (var i = 0; i < iterations; i++) {
            extras[i] = randomPosition(width, height, offset);
        }

        return extras;
    }

    static int randomPosition(int width, int height, int offset) {
        return (int) (Math.floor(Math.random() * (width - 2 * offset + 1) + offset) +  Math.floor(Math.random() * (height - 2 * offset + 1) + offset) * height);
    }
}
