package io.github.lain2lo.ktgmonitor.application.service.metrics;

import io.github.lain2lo.ktgmonitor.application.service.MetricCalculator;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class UterineActivityCalculator implements MetricCalculator {

    @Override
    public String getName() {
        return "uterineActivity";
    }

    @Override
    public Mono<Double> compute(Flux<Sample> window) {
        return window.collectList().map(this::analyzeUterineActivity);
    }

    private Double analyzeUterineActivity(List<Sample> samples) {
        if (samples.isEmpty()) return 0.0;

        int contractions = 0;
        double totalAmplitude = 0.0;

        boolean inContraction = false;
        double baseline = samples.stream()
                .mapToDouble(s -> s.uterus() != null ? s.uterus() : 0.0)
                .average().orElse(0.0);

        for (Sample s : samples) {
            if (s.uterus() == null) continue;
            double value = s.uterus();

            // считаем схваткой превышение baseline + 15 мм рт. ст.
            if (!inContraction && value > baseline + 15) {
                inContraction = true;
                contractions++;
            }

            if (inContraction && value <= baseline + 5) {
                inContraction = false;
            }

            if (inContraction) {
                totalAmplitude += (value - baseline);
            }
        }

        double avgAmplitude = contractions > 0 ? totalAmplitude / contractions : 0.0;

        // Можно возвращать интегральный индекс: кол-во схваток * средн. амплитуда
        return contractions + avgAmplitude / 10.0;
    }
}