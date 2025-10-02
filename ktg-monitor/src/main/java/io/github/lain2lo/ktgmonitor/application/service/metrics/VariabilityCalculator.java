package io.github.lain2lo.ktgmonitor.application.service.metrics;

import io.github.lain2lo.ktgmonitor.application.service.MetricCalculator;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.DoubleSummaryStatistics;
import java.util.Objects;

@Component
public class VariabilityCalculator implements MetricCalculator {

    @Override
    public String getName() {
        return "variability";
    }

    @Override
    public Mono<Double> compute(Flux<Sample> window) {
        return window
                .map(Sample::bpm)
                .filter(Objects::nonNull)
                .collectList()
                .map(values -> {
                    if (values.isEmpty()) return 0.0;

                    // STV = среднее |x[i+1] - x[i]|
                    double stv = 0.0;
                    for (int i = 1; i < values.size(); i++) {
                        stv += Math.abs(values.get(i) - values.get(i - 1));
                    }
                    stv /= values.size();

                    // LTV = стандартное отклонение
                    DoubleSummaryStatistics stats = values.stream()
                            .mapToDouble(value -> value)
                            .summaryStatistics();

                    double mean = stats.getAverage();
                    double variance = values.stream()
                            .mapToDouble(v -> Math.pow(v - mean, 2))
                            .sum() / values.size();

                    double ltv = Math.sqrt(variance);

                    // можно вернуть комбинированный индекс (например, stv + ltv)
                    return (stv + ltv) / 2.0;
                });
    }
}