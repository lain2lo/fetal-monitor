package io.github.lain2lo.ktgmonitor.infrastructure.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import io.github.lain2lo.ktgmonitor.infrastructure.socket.UnixSocketServer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RecordWebSocketHandler implements WebSocketHandler {

    private final Flux<Sample> sampleFlux;
    private final ObjectMapper mapper = new ObjectMapper();

    public RecordWebSocketHandler(UnixSocketServer socketServer) {
        this.sampleFlux = socketServer.flux();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<Sample> clientFlux = sampleFlux
                .onBackpressureBuffer(500);

        return session.send(
                clientFlux.handle((record, sink) -> {
                    try {
                        sink.next(session.textMessage(mapper.writeValueAsString(record)));
                    } catch (Exception e) {
                        sink.error(new RuntimeException(e));
                    }
                })
        );
    }
}
