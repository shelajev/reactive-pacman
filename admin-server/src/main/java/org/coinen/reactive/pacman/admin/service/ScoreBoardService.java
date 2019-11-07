package org.coinen.reactive.pacman.admin.service;

import java.util.List;
import java.util.Map;

public interface ScoreBoardService {

    void reset();

    List<Map.Entry<String, Integer>> score();
}
