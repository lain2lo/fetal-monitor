package io.github.lain2lo.ktgmonitor.api.controller;

import io.github.lain2lo.ktgmonitor.application.service.InferenceService;
import io.github.lain2lo.ktgmonitor.domain.InferenceResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/sse/inference")
public class InferenceSseController {

    private final InferenceService service;

    public InferenceSseController(InferenceService service) {
        this.service = service;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InferenceResult> stream() {
        return service.streamResults();
    }

    @GetMapping("/latest")
    public Mono<InferenceResult> latest() {
        return service.latest();
    }
}