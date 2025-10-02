package io.github.lain2lo.ktgmonitor.application.service.metrics;

import io.github.lain2lo.ktgmonitor.application.service.MetricCalculator;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class DecelerationsCalculator implements MetricCalculator {

    @Override
    public String getName() {
        return "decelerations";
    }

    @Override
    public Mono<Double> compute(Flux<Sample> window) {
        return window
                .map(Sample::bpm)
                .filter(Objects::nonNull)
                .collectList()
                .map(list -> {
                    int count = 0;
                    for (int i = 1; i < list.size(); i++) {
                        // если падение bpm более 15 ударов относительно предыдущего, считаем децелерацией
                        if (list.get(i - 1) - list.get(i) >= 15) count++;
                    }
                    return (double) count;
                });
    }
}