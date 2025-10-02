package io.github.lain2lo.ktgmonitor.application.service;

import ai.onnxruntime.OrtException;
import io.github.lain2lo.ktgmonitor.domain.BpmPredictionResult;
import io.github.lain2lo.ktgmonitor.infrastructure.ml.ONNXInferenceBpmPredictEngine;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class BpmPredictionService {

    private final SampleService sampleService;
    private final ONNXInferenceBpmPredictEngine engine;
    private final Sinks.Many<BpmPredictionResult> sink = Sinks.many().multicast().onBackpressureBuffer();
    private volatile BpmPredictionResult latest;

    public BpmPredictionService(SampleService sampleService) throws OrtException {
        this.sampleService = sampleService;
        this.engine = new ONNXInferenceBpmPredictEngine("./seq2seq_v0_30.09.25.onnx");
    }

    @PostConstruct
    public void init() {
        int WINDOW_SIZE = 1000;
        sampleService.streamAll()
                .buffer(WINDOW_SIZE)
                .subscribe(batch -> {
                    if (!batch.isEmpty()) {
                        BpmPredictionResult result = engine.run(batch);
                        latest = result;
                        sink.tryEmitNext(result);
                        System.out.println("PredictedBpmResult: " + result);
                    }
                });
    }

    public Flux<BpmPredictionResult> streamResults() {
        return sink.asFlux();
    }

    public Flux<BpmPredictionResult> latestResult() {
        if (latest != null) {
            return Flux.just(latest);
        } else {
            return Flux.empty();
        }
    }
}