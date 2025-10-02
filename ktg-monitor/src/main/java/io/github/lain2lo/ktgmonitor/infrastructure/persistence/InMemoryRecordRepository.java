package io.github.lain2lo.ktgmonitor.infrastructure.persistence;

import io.github.lain2lo.ktgmonitor.application.port.SampleRepository;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Repository
public class InMemoryRecordRepository implements SampleRepository {

    private final Sinks.Many<Sample> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public void save(Sample sample) {
        sink.tryEmitNext(sample);
    }

    @Override
    public Flux<Sample> streamAll() {
        return sink.asFlux();
    }
}
