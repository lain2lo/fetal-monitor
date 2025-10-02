package io.github.lain2lo.ktgmonitor.application.service.metrics;

import io.github.lain2lo.ktgmonitor.application.service.MetricCalculator;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class BaselineCalculator implements MetricCalculator {

    @Override
    public String getName() {
        return "baseline";
    }

    @Override
    public Mono<Double> compute(Flux<Sample> window) {
        return window
                .map(Sample::bpm)
                .filter(Objects::nonNull)
                .collectList()
                .map(list -> list.stream().mapToDouble(value -> value).average().orElse(0.0));
    }
}