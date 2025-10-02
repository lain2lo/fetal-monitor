package io.github.lain2lo.ktgmonitor.infrastructure.socket;

import io.github.lain2lo.ktgmonitor.application.service.SampleService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class SocketToDomainBridge {
    private final UnixSocketServer socketServer;
    private final SampleService sampleService;

    public SocketToDomainBridge(UnixSocketServer socketServer, SampleService sampleService) {
        this.socketServer = socketServer;
        this.sampleService = sampleService;
    }

    @PostConstruct
    public void init() {
        socketServer.flux()
                .subscribe(sampleService::save,
                        err -> System.err.println("Error parsing record: " + err));
    }

}