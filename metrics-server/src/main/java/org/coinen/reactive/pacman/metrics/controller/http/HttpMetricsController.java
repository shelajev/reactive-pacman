package org.coinen.reactive.pacman.metrics.controller.http;

import com.google.protobuf.Empty;
import io.micrometer.core.instrument.MeterRegistry;
import org.coinen.pacman.metrics.MetricsSnapshot;
import org.coinen.reactive.pacman.metrics.MappingUtils;
import org.coinen.reactive.pacman.metrics.service.MetricsService;
import reactor.core.publisher.Flux;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/http")
public class HttpMetricsController {

    final MetricsService metricsService;
    final MeterRegistry registry;

    public HttpMetricsController(MetricsService service, MeterRegistry registry) {
        this.metricsService = service;
        this.registry = registry;
    }

    @PostMapping("/metrics")
    @CrossOrigin(origins = "*", methods = RequestMethod.POST, allowedHeaders = "*", allowCredentials = "true")
    public Empty metrics(@RequestBody MetricsSnapshot metricsSnapshot) {
//        metricsService.metrics(
//            Flux.fromStream(metricsSnapshot.getMetersList()
//                                           .stream()
//                                           .map(MappingUtils::mapMeter))
//        );

        return Empty.getDefaultInstance();
    }
}
