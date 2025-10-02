// infrastructure/repository/InMemoryInferenceRepository.java
package io.github.lain2lo.ktgmonitor.infrastructure.persistence;

import io.github.lain2lo.ktgmonitor.application.port.InferenceRepository;
import io.github.lain2lo.ktgmonitor.domain.InferenceResult;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryInferenceRepository implements InferenceRepository {

    private final List<InferenceResult> storage = new CopyOnWriteArrayList<>();

    @Override
    public Mono<Void> save(InferenceResult result) {
        storage.add(result);
        return Mono.empty();
    }

    @Override
    public Flux<InferenceResult> findAll() {
        return Flux.fromIterable(storage);
    }

    @Override
    public Mono<Long> count() {
        return Mono.just((long) storage.size());
    }
}
