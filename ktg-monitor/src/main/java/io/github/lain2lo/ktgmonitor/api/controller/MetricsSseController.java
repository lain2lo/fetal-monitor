package io.github.lain2lo.ktgmonitor.api.controller;

import io.github.lain2lo.ktgmonitor.application.service.MetricCalculator;
import io.github.lain2lo.ktgmonitor.application.service.SampleService;
import io.github.lain2lo.ktgmonitor.domain.KeyMetrics;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/sse")
public class MetricsSseController {
    private final List<MetricCalculator> calculators;
    private final SampleService sampleService;
    private final int WINDOW_SIZE = 1000;
    private final LinkedList<Sample> windowBuffer = new LinkedList<>();
    private final Map<String, LinkedList<Double>> lastValues = new ConcurrentHashMap<>();

    public MetricsSseController(List<MetricCalculator> calculators, SampleService sampleService) {
        this.calculators = calculators;
        this.sampleService = sampleService;
    }

    @GetMapping(value = "/metrics", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<KeyMetrics> streamMetrics() {
        return sampleService.streamAll()
                .doOnNext(sample -> {
                    windowBuffer.addLast(sample);
                    if (windowBuffer.size() > WINDOW_SIZE) windowBuffer.removeFirst();
                })
                .sample(Duration.ofSeconds(5))
                .flatMap(tick -> {
                    List<Sample> snapshot = List.copyOf(windowBuffer);

                    return Flux.fromIterable(calculators)
                            .flatMap(calc ->
                                    calc.compute(Flux.fromIterable(snapshot))
                                            .map(value -> new Object[]{calc.getName(), value})
                                            .subscribeOn(Schedulers.boundedElastic())
                            )
                            .collectMap(arr -> (String) arr[0], arr -> (Double) arr[1])
                            .map(results -> {
                                KeyMetrics km = new KeyMetrics();

                                results.forEach((name, value) -> {
                                    String status = evaluateStatus(name, value);

                                    // --- вычисляем тренд по последним значениям ---
                                    LinkedList<Double> history = lastValues.computeIfAbsent(name, k -> new LinkedList<>());
                                    if (value != null) {
                                        history.addLast(value);
                                        if (history.size() > 5) history.removeFirst();
                                    }
                                    String trend = evaluateTrend(history);

                                    km.addMetric(name, value, status, trend);
                                });

                                km.setPrognosticScore("Fisher", calculateFisher(results));
                                km.setPrognosticScore("Krebs", calculateKrebs(results));

                                return km;
                            });
                });
    }

    private String evaluateStatus(String metric, Double value) {
        if (value == null) return "UNKNOWN";
        return switch (metric) {
            case "baseline" -> (value >= 110 && value <= 160) ? "NORMAL" : "PATHOLOGICAL";
            case "variability" -> (value >= 5 && value <= 25) ? "NORMAL" : "SUSPICIOUS";
            case "accelerations" -> (value > 0) ? "NORMAL" : "SUSPICIOUS";
            case "decelerations" -> (value == 0) ? "NORMAL" : "PATHOLOGICAL";
            case "uterineActivity" -> (value <= 5) ? "NORMAL" : "SUSPICIOUS";
            case "baselineStability" -> (value <= 5) ? "NORMAL" : (value <= 10 ? "SUSPICIOUS" : "PATHOLOGICAL");
            default -> "UNKNOWN";
        };
    }

    private String evaluateTrend(LinkedList<Double> history) {
        if (history.size() < 2) return "STABLE";
        double last = history.getLast();
        double prev = history.get(history.size() - 2);
        if (last > prev) return "UP";
        if (last < prev) return "DOWN";
        return "STABLE";
    }

    // 🔹 Фишер: от 0 до 10
    private int calculateFisher(java.util.Map<String, Double> metrics) {
        int score = 0;
        Double baseline = metrics.get("baseline");
        Double variability = metrics.get("variability");
        Double accelerations = metrics.get("accelerations");
        Double decelerations = metrics.get("decelerations");

        // baseline (0–3)
        if (baseline != null) {
            if (baseline >= 110 && baseline <= 160) score += 3;
            else if ((baseline >= 100 && baseline < 110) || (baseline > 160 && baseline <= 180)) score += 2;
            else score += 0;
        }

        // variability (0–3)
        if (variability != null) {
            if (variability >= 6 && variability <= 25) score += 3;
            else if (variability >= 3 && variability < 6) score += 2;
            else score += 0;
        }

        // accelerations (0–2)
        if (accelerations != null) {
            if (accelerations >= 2) score += 2;
            else if (accelerations == 1) score += 1;
        }

        // decelerations (0–2)
        if (decelerations != null) {
            if (decelerations == 0) score += 2;
            else if (decelerations <= 2) score += 1;
        }

        return Math.min(score, 10); // гарантируем максимум 10
    }

    // 🔹 Krebs: 0–12
    private int calculateKrebs(java.util.Map<String, Double> metrics) {
        int score = calculateFisher(metrics); // база Fisher

        Double uterine = metrics.get("uterineActivity");
        // uterine activity (0–2)
        if (uterine != null) {
            if (uterine <= 5) score += 2;
            else if (uterine <= 7) score += 1;
        }

        return Math.min(score, 12); // гарантируем максимум 12
    }

}