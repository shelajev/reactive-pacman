package org.coinen.reactive.pacman.metrics.service.support;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Empty;
import org.coinen.pacman.Player;
import org.coinen.pacman.PlayerServiceClient;
import org.coinen.reactive.pacman.metrics.service.ScoreBoardService;
import reactor.core.publisher.BaseSubscriber;

public class DefaultScoreBoardService extends BaseSubscriber<Player> implements ScoreBoardService {


    final Map<String, Integer> lastScoreStatistic;
    final Map<String, Integer> scoreBoard;

    public DefaultScoreBoardService(PlayerServiceClient playerServiceClient) {
        lastScoreStatistic = new HashMap<>();
        scoreBoard = new HashMap<>();

        playerServiceClient.players(Empty.getDefaultInstance())
                           .subscribe(this);
    }

    @Override
    public List<Map.Entry<String, Integer>> score() {
        List<Map.Entry<String, Integer>> result =
            new ArrayList<>(scoreBoard.entrySet());

        result.sort(Comparator.<Map.Entry<String, Integer>, Integer>comparing(Map.Entry::getValue).reversed());

        return result;
    }

    @Override
    protected void hookOnNext(Player value) {
        String nickname = value.getNickname();
        if (value.getState() == Player.State.CONNECTED) {
            lastScoreStatistic.put(nickname, 0);
            scoreBoard.putIfAbsent(nickname, 0);
        } else if (value.getState() == Player.State.DISCONNECTED) {
            scoreBoard.compute(nickname, (s, score) -> {
                if (score != null) {
                    int nextScore = score - 5;

                    return Math.max(nextScore, 0);
                }

                return score;
            });
        } else {
            Integer lastScore = lastScoreStatistic.getOrDefault(nickname, 0);
            int newScore = value.getScore();

            if (lastScore == newScore) {
                return;
            }
            scoreBoard.compute(nickname, (s, score) -> {
                if (score != null) {
                    return score + (newScore - lastScore);
                }

                return newScore;
            });
            lastScoreStatistic.put(nickname, newScore);
        }
    }
}
