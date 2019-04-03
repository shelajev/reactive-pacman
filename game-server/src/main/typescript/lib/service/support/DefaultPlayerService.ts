import PlayerService from '../PlayerService';
import { DirectProcessor, Flux, Mono } from 'reactor-core-js/flux';
import { Player } from '@shared/player_pb';
import { ExtrasService, MapService} from '../';
import { Location, Direction } from '@shared/location_pb';
import { PlayerRepository } from '../../repository/';
import { Point } from '@shared/point_pb';
import DefaultMapService from './DefaultMapService';

export default class DefaultPlayerService implements PlayerService {

  private playersProcessor = new DirectProcessor<Player>();

  constructor(
    private playerRepository: PlayerRepository,
    private extrasService: ExtrasService,
    private mapService: MapService
  ) {
    // setInterval(this.checkPlayers.bind(this), 5000);
  }

  checkPlayers(el: number): void {
    this.playerRepository.findAll()
      .forEach((p: Player) => {
        if (new Date().getMilliseconds() - p.getTimestamp() > 60000) {
          this.disconnectPlayer(p.getUuid());
        }
      });
  }

  locate(uuid: string, locationStream: Flux<Location>): Flux<Location> {
    return locationStream.doOnNext(location => {
      console.log('location', location.toObject());
      const time = new Date().getMilliseconds();
      const updatedPlayer = this.playerRepository.update(uuid, (player: Player) => {
        const foundPlayer = this.playerRepository.findOne(uuid);

        if (!foundPlayer) {
          return null;
        }

        const pl = player;
        pl.setTimestamp(time);
        pl.setLocation(location);

        const position: Point = location.getPosition();
        const collisions: Player[] = this.playerRepository.findAll()
          .filter(p => p.getState() === Player.State.ACTIVE)
          .filter(p => p.getType() !== player.getType())
          .filter(p => DefaultMapService.distance2(
              p.getLocation().getPosition(),
              position
            ) < 100
          );
          if (collisions.length > 0) {
            if (
              this.extrasService.isPowerupActive() && player.getType() === Player.Type.GHOST ||
              !this.extrasService.isPowerupActive() && player.getType() === Player.Type.PACMAN) {
                console.log('colided 1');
                pl.setState(Player.State.DISCONNECTED);
                collisions.forEach(collision => {
                  const collidedWith = this.playerRepository
                    .update(collision.getUuid(), (p: Player) => {
                      p.setScore(p.getScore() + 100);
                      return p;
                    });
                  this.playersProcessor.onNext(collidedWith);
                });
          } else if (
            this.extrasService.isPowerupActive() && player.getType() === Player.Type.PACMAN
            || !this.extrasService.isPowerupActive() && player.getType() === Player.Type.GHOST) {
              console.log('colieded 2');
              collisions.forEach(collision => {
                const collidedWith = this.playerRepository
                  .update(collision.getUuid(), (p: Player) => {
                    p.setState(Player.State.DISCONNECTED);
                    return p;
                  });
                this.playersProcessor.onNext(collidedWith);
              });
              pl.setScore(player.getScore() + 100 * collisions.length);
            }
          } else if (player.getType() === Player.Type.PACMAN && this.extrasService.check(
            position.getX(),
            position.getY()) > 0) {
              console.log('increment score');
              pl.setScore(player.getScore() + 1);
          }
          pl.setTimestamp(new Date().getMilliseconds());
          return pl;
      });
      if (updatedPlayer.getState() === Player.State.DISCONNECTED) {
        this.playerRepository.delete(uuid);
      }

      this.playersProcessor.onNext(updatedPlayer);
    });
  }

  players(): Flux<Player> {
    return this.playersProcessor;
  }

  createPlayer(uuid: string, nickname: string) {
    return this.playerRepository.save(uuid, () => {
      const playerType: Player.Type = this.generatePlayerType();
      const bestPosition: Point = this.findBestStartingPosition(playerType);
      const playerPosition: Point = new Point();
      playerPosition.setX(bestPosition.getX() * 100);
      playerPosition.setY(bestPosition.getY() * 100);
      const location = new Location();
      location.setDirection(Direction.RIGHT);
      location.setPosition(playerPosition);
      const player = new Player();
      player.setScore(0);
      player.setType(playerType);
      player.setLocation(location);
      player.setNickname(nickname);
      player.setState(Player.State.CONNECTED);
      player.setUuid(uuid);
      player.setTimestamp(new Date().getMilliseconds());
      return player;
    });
  }

  disconnectPlayer(uuid: string): void {
    const player = this.playerRepository.delete(uuid);
    if (player) {
      player.setState(Player.State.DISCONNECTED);
      this.playersProcessor.onNext(player);
    }
  }

  generatePlayerType(): Player.Type {
    let manCount = 0;
    let ghostCount = 0;
    const players = this.playerRepository.findAll();

    for (let player of players) {
      if (player.getType() === Player.Type.PACMAN) {
        manCount++;
      } else if (player.getType() === Player.Type.GHOST) {
        ghostCount++;
      }
    }
    if (ghostCount < manCount) {
        return Player.Type.GHOST;
    }
    else {
        return Player.Type.PACMAN;
    }
  }

  private findBestStartingPosition(playerType: Player.Type) {
    const players = this.playerRepository.findAll()
      .filter(p => p.getType() !== playerType);

    while (true) {
      let point = this.mapService.getRandomPoint();
      if (players.length === 0) {
        return point;
      }
      for (let player of players) {
        if (playerType !== player.getType()) {
          const dist = DefaultMapService.distance2(player.getLocation().getPosition(), point);
          if (dist > 5) {
            return point;
          }
        }
      }
    }
  }
    
}
