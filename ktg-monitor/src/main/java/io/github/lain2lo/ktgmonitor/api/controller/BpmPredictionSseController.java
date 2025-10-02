package io.github.lain2lo.ktgmonitor.api.controller;

import io.github.lain2lo.ktgmonitor.application.service.BpmPredictionService;
import io.github.lain2lo.ktgmonitor.domain.BpmPredictionResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/sse/bpm-predict")
public class BpmPredictionSseController {

    private final BpmPredictionService service;

    public BpmPredictionSseController(BpmPredictionService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BpmPredictionResult> streamBpmPrediction() {
        return service.streamResults();
    }

    @GetMapping("/latest")
    public Flux<BpmPredictionResult> latestResult() {
        return service.latestResult();
    }
}