package org.coinen.reactive.pacman.metrics.service;

import java.util.List;
import java.util.Map;

public interface ScoreBoardService {


    List<Map.Entry<String, Integer>> score();
}
