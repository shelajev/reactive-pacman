package org.coinen.reactive.pacman.reactivepacman.config;

import org.coinen.reactive.pacman.reactivepacman.repository.ExtrasRepository;
import org.coinen.reactive.pacman.reactivepacman.repository.support.InMemoryExtrasRepository;
import org.coinen.reactive.pacman.reactivepacman.repository.support.InMemoryPlayerRepository;
import org.coinen.reactive.pacman.reactivepacman.repository.PlayerRepository;
import org.coinen.reactive.pacman.reactivepacman.service.ExtrasService;
import org.coinen.reactive.pacman.reactivepacman.service.GameService;
import org.coinen.reactive.pacman.reactivepacman.service.MapService;
import org.coinen.reactive.pacman.reactivepacman.service.PlayerService;
import org.coinen.reactive.pacman.reactivepacman.service.support.DefaultExtrasService;
import org.coinen.reactive.pacman.reactivepacman.service.support.DefaultGameService;
import org.coinen.reactive.pacman.reactivepacman.service.support.DefaultMapService;
import org.coinen.reactive.pacman.reactivepacman.service.support.DefaultPlayerService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultApplicationConfig {

    @Bean
    public ExtrasRepository extrasRepository() {
        return new InMemoryExtrasRepository();
    }

    @Bean
    public PlayerRepository playerRepository() {
        return new InMemoryPlayerRepository();
    }

    @Bean
    public ExtrasService extrasService(ExtrasRepository extrasRepository, PlayerRepository playerRepository) {
        return new DefaultExtrasService(extrasRepository, playerRepository);
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
    public PlayerService playerService(PlayerRepository playerRepository,
        ExtrasService extrasService, MapService mapService) {
        return new DefaultPlayerService(playerRepository, extrasService, mapService);
    }
}
