package io.github.lain2lo.ktgmonitor.application.port;

import io.github.lain2lo.ktgmonitor.domain.Sample;
import reactor.core.publisher.Flux;

public interface SampleRepository {
    void save(Sample sample);

    Flux<Sample> streamAll();
}
