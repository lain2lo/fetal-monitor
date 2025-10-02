package io.github.lain2lo.ktgmonitor.application.service;

import ai.onnxruntime.OrtException;
import io.github.lain2lo.ktgmonitor.application.port.InferenceRepository;
import io.github.lain2lo.ktgmonitor.domain.InferenceResult;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import io.github.lain2lo.ktgmonitor.infrastructure.ml.ONNXInferenceHypoxiaEngine;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class InferenceService {

    private final ONNXInferenceHypoxiaEngine engine;
    private final SampleService sampleService;
    private final InferenceRepository inferenceRepository;

    private final Sinks.Many<InferenceResult> sink = Sinks.many().replay().latest();
    private final AtomicReference<InferenceResult> lastResult = new AtomicReference<>();

    private final int WINDOW_SIZE = 10000;
    private final LinkedList<Sample> buffer = new LinkedList<>();

    public InferenceService(SampleService sampleService,
                            InferenceRepository inferenceRepository) throws OrtException {
        this.engine = new ONNXInferenceHypoxiaEngine("./model_v0_28.09.25.onnx");
        this.sampleService = sampleService;
        this.inferenceRepository = inferenceRepository;
    }

    @PostConstruct
    public void init() {
        sampleService.streamAll()
                .doOnNext(sample -> {
                    buffer.addLast(sample);
                    if (buffer.size() > WINDOW_SIZE) {
                        buffer.removeFirst();
                    }
                })
                .sample(Duration.ofSeconds(60))
                .subscribe(batch -> {
                    String patientId = buffer.getLast().patientId();
                    InferenceResult result = engine.run(patientId, new ArrayList<>(buffer));

                    inferenceRepository.save(result).subscribe();
                    lastResult.set(result);
                    sink.tryEmitNext(result);
                });
    }

    /**
     * Последний результат
     */
    public Mono<InferenceResult> latest() {
        return Mono.justOrEmpty(lastResult.get());
    }

    /**
     * SSE поток
     */
    public Flux<InferenceResult> streamResults() {
        return sink.asFlux();
    }

    /**
     * История
     */
    public Flux<InferenceResult> history() {
        return inferenceRepository.findAll();
    }
}
