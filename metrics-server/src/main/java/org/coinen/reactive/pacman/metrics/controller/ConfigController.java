package org.coinen.reactive.pacman.metrics.controller;

import org.coinen.reactive.pacman.metrics.service.support.FastInfluxMetricsBridgeService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class ConfigController {

    final FastInfluxMetricsBridgeService fastInfluxMetricsBridgeService;

    public ConfigController(FastInfluxMetricsBridgeService service) {
        fastInfluxMetricsBridgeService = service;
    }

    @GetMapping
    public void configure(
        @RequestParam(value = "capacity", required = false) Long capacity,
        @RequestParam(value = "threshold", required = false) Long threshold) {
        fastInfluxMetricsBridgeService.configure(capacity, threshold);
    }
}
