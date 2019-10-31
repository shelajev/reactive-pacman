package org.coinen.reactive.pacman.metrics.controller.http;

import java.util.List;
import java.util.Map;

import org.coinen.reactive.pacman.metrics.service.ScoreBoardService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class ScoreBoardController {

    final ScoreBoardService scoreBoardService;

    public ScoreBoardController(ScoreBoardService service) {
        this.scoreBoardService = service;
    }


    @PostMapping("/score/reset")
    @CrossOrigin(origins = "*", methods = RequestMethod.POST, allowedHeaders = "*", allowCredentials = "true")
    public void resetScore() {
        scoreBoardService.reset();
    }

    @GetMapping("/score")
    @CrossOrigin(origins = "*", methods = RequestMethod.GET, allowedHeaders = "*", allowCredentials = "true")
    public List<Map.Entry<String, Integer>> score() {
        return scoreBoardService.score();
    }
}
