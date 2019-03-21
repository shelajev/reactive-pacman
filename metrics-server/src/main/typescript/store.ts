import { Player } from '@shared/player_pb';
import { Score } from '@shared/score_pb';
import Maze from './maze';

const store = {
    players: <{ [e: string]: Player }> null,
    maze: <Maze> null,
    powerUpEnd: 0,
    leaderBoard: Array<Score>(),
    leaderBoardIndexes: new Map<String, integer>()
}


const swapWithPreceeding = (index: integer) => {
    let temp = store.leaderBoard[index];
    store.leaderBoard[index] = store.leaderBoard[index - 1];
    store.leaderBoard[index - 1] = temp;
    store.leaderBoardIndexes.set(store.leaderBoard[index].getUsername(), index);
    store.leaderBoardIndexes.set(store.leaderBoard[index - 1].getUsername(), index - 1);
}

const incrementLeaderboard = (index: integer, val: integer) => {
    for(let i = index; i > 1; i--) {
        if (store.leaderBoard[i] > store.leaderBoard[i - 1]) {
            swapWithPreceeding(i);
        } else {
            break;
        }
    }
}

const accessors = {
    getPlayers: () => store.players,
    getPlayer: (uuid: string) => store.players[uuid],
    getMaze: () => store.maze,
    getPowerUpEnd: () => store.powerUpEnd,
    getLeaderBoard: () => store.leaderBoard,
    getLeaderBoardIndex: (player: String) => store.leaderBoardIndexes.get(player),
    getScore: (player: String) => store.leaderBoard[store.leaderBoardIndexes.get(player)],
    setPlayers: (value: { [e: string]: Player }) => store.players = value,
    setPlayer: (value: Player) => store.players[value.getUuid()] = value,
    setMaze: (value: Maze) => store.maze = value,
    setPowerUpEnd: (value: integer) => store.powerUpEnd = value,
    incrementScore: (player: String, value: integer) => {
        let index = this.getLeaderBoardIndex(player);
        incrementLeaderboard(index, value);
    }
}



export default accessors;
