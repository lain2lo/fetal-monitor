package io.github.lain2lo.ktgmonitor.application.service;

import io.github.lain2lo.ktgmonitor.domain.Sample;
import io.github.lain2lo.ktgmonitor.application.port.SampleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class SampleService {
    private final SampleRepository repository;

    public SampleService(SampleRepository repository) {
        this.repository = repository;
    }

    public void save(Sample sample) {
        repository.save(sample);
    }

    public Flux<Sample> streamAll() {
        return repository.streamAll();
    }
}
