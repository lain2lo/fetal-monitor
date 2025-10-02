package io.github.lain2lo.ktgmonitor.api.controller;

import io.github.lain2lo.ktgmonitor.api.dto.SampleDto;
import io.github.lain2lo.ktgmonitor.application.service.SampleService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/samples")
public class SampleController {
    private final SampleService service;

    public SampleController(SampleService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<SampleDto> streamAll() {
        return service.streamAll().map(r -> new SampleDto(r.timeSec(), r.bpm(), r.uterus(), r.patientId()));
    }
}
