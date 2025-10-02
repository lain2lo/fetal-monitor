package io.github.lain2lo.ktgmonitor.application.port;

import io.github.lain2lo.ktgmonitor.domain.InferenceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InferenceRepository {
    Mono<Void> save(InferenceResult result);

    Flux<InferenceResult> findAll();

    Mono<Long> count();
}
