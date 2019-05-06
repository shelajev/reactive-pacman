package org.coinen.reactive.pacman.config;

import org.coinen.reactive.pacman.repository.ExtrasRepository;
import org.coinen.reactive.pacman.repository.PlayerRepository;
import org.coinen.reactive.pacman.repository.support.InMemoryExtrasRepository;
import org.coinen.reactive.pacman.repository.support.InMemoryPlayerRepository;
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
