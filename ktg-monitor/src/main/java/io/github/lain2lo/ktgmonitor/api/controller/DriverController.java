package io.github.lain2lo.ktgmonitor.api.controller;

import io.github.lain2lo.ktgmonitor.infrastructure.socket.UnixSocketServer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/driver")
public class DriverController {

    private final UnixSocketServer server; // твой драйвер/сервер

    public DriverController(UnixSocketServer server) {
        this.server = server;
    }

    @PostMapping("/start")
    public ResponseEntity<String> start() {
        server.startScanning(); // метод, который запускает
        return ResponseEntity.ok("Сканирование запущено");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        server.stopScanning(); // метод, который останавливает
        return ResponseEntity.ok("Сканирование остановлено");
    }
}