package io.github.lain2lo.ktgmonitor.application.service;

import io.github.lain2lo.ktgmonitor.domain.Sample;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MetricCalculator {
    String getName();
    Mono<Double> compute(Flux<Sample> window);
}