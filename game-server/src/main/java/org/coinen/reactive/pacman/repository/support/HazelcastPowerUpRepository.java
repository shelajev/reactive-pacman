package org.coinen.reactive.pacman.repository.support;

import java.time.Duration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import org.coinen.reactive.pacman.repository.PowerRepository;
import reactor.core.publisher.Mono;

public class HazelcastPowerUpRepository implements PowerRepository {

    final IAtomicLong powerUpCounter;

    public HazelcastPowerUpRepository(HazelcastInstance hazelcastInstance) {
        this.powerUpCounter = hazelcastInstance.getAtomicLong("powerUpCounter");
    }

    @Override
    public boolean isPowerUp() {
        return powerUpCounter.get() > 0;
    }

    @Override
    public void powerUp() {
        powerUpCounter.incrementAndGetAsync();
        Mono.delay(Duration.ofSeconds(10))
            .subscribe(__ -> powerUpCounter.decrementAndGetAsync());
    }
}
