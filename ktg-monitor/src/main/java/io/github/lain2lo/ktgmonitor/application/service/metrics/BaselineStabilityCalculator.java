package io.github.lain2lo.ktgmonitor.application.service.metrics;

import io.github.lain2lo.ktgmonitor.application.service.MetricCalculator;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
public class BaselineStabilityCalculator implements MetricCalculator {

    @Override
    public String getName() {
        return "baselineStability";
    }

    @Override
    public Mono<Double> compute(Flux<Sample> window) {
        return window.collectList().map(this::computeStability);
    }

    private Double computeStability(List<Sample> samples) {
        if (samples.isEmpty()) return 0.0;

        // делим окно на части (например, по 2 минуты)
        int chunkSize = samples.size() / 5;
        if (chunkSize == 0) return 0.0;

        double prevBaseline = -1;
        double totalDiff = 0.0;
        int count = 0;

        for (int i = 0; i < samples.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, samples.size());
            List<Sample> chunk = samples.subList(i, end);

            double baseline = chunk.stream()
                    .map(Sample::bpm)
                    .filter(Objects::nonNull)
                    .mapToDouble(value -> value)
                    .average().orElse(0.0);

            if (prevBaseline > 0) {
                totalDiff += Math.abs(baseline - prevBaseline);
                count++;
            }
            prevBaseline = baseline;
        }

        return count > 0 ? totalDiff / count : 0.0;
    }
}
