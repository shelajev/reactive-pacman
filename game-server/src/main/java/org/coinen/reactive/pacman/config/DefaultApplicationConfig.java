package org.coinen.reactive.pacman.config;

import com.hazelcast.core.HazelcastInstance;
import org.coinen.reactive.pacman.repository.ExtrasRepository;
import org.coinen.reactive.pacman.repository.PlayerRepository;
import org.coinen.reactive.pacman.repository.PowerRepository;
import org.coinen.reactive.pacman.repository.support.HazelcastExtrasRepository;
import org.coinen.reactive.pacman.repository.support.HazelcastPlayerRepository;
import org.coinen.reactive.pacman.repository.support.HazelcastPowerUpRepository;
import org.coinen.reactive.pacman.service.ExtrasService;
import org.coinen.reactive.pacman.service.GameService;
import org.coinen.reactive.pacman.service.MapService;
import org.coinen.reactive.pacman.service.PlayerService;
import org.coinen.reactive.pacman.service.support.DefaultExtrasService;
import org.coinen.reactive.pacman.service.support.DefaultGameService;
import org.coinen.reactive.pacman.service.support.DefaultMapService;
import org.coinen.reactive.pacman.service.support.DefaultPlayerService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultApplicationConfig {

    @Bean
    public ExtrasRepository extrasRepository(HazelcastInstance hazelcastInstance) {
        return new HazelcastExtrasRepository(hazelcastInstance);
    }

    @Bean
    public PlayerRepository playerRepository(HazelcastInstance hazelcastInstance) {
        return new HazelcastPlayerRepository(hazelcastInstance);
    }

    @Bean
    public PowerRepository powerRepository(HazelcastInstance hazelcastInstance) {
        return new HazelcastPowerUpRepository(hazelcastInstance);
    }

    @Bean
    public ExtrasService extrasService(ExtrasRepository extrasRepository,
        PlayerRepository playerRepository, PowerRepository powerRepository) {
        return new DefaultExtrasService(extrasRepository, playerRepository, powerRepository);
    }

    @Bean
    public GameService gameService(PlayerService playerService,
        ExtrasRepository extrasRepository, PlayerRepository playerRepository,
        MapService mapService) {
        return new DefaultGameService(playerService, extrasRepository,
            playerRepository, mapService);
    }

    @Bean
    public MapService mapService() {
        return new DefaultMapService();
    }

    @Bean
    public PlayerService playerService(
        PlayerRepository playerRepository,
        ExtrasService extrasService,
        MapService mapService,
        PowerRepository powerRepository
    ) {
        return new DefaultPlayerService(playerRepository, extrasService, mapService, powerRepository);
    }
}
