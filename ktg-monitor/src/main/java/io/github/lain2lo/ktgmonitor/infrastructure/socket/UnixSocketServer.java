package io.github.lain2lo.ktgmonitor.infrastructure.socket;

import io.github.lain2lo.ktgmonitor.domain.Sample;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class UnixSocketServer {

    private final Sinks.Many<Sample> sink = Sinks.many().multicast().onBackpressureBuffer();
    private ExecutorService executor;
    private Future<?> serverFuture;

    private Process pythonProcess;
    private ExecutorService logExecutor;

    @PostConstruct
    public void init() {
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "UnixSocketServer-Thread"));
        logExecutor = Executors.newCachedThreadPool(r -> new Thread(r, "Python-Log-Thread"));
        serverFuture = executor.submit(this::startServer);
    }

    private void startServer() {
        File socketFile = new File("/tmp/ktg.sock");
        if (socketFile.exists()) socketFile.delete();

        try (AFUNIXServerSocket server = AFUNIXServerSocket.newInstance()) {
            server.bind(AFUNIXSocketAddress.of(socketFile));

            while (!Thread.currentThread().isInterrupted()) {
                try (var sock = server.accept();
                     InputStream in = sock.getInputStream()) {

                    byte[] buf = new byte[16]; // ts + bpm + uterus
                    while (true) {
                        int read = in.readNBytes(buf, 0, buf.length);
                        if (read < buf.length) break;

                        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
                        long ts = System.currentTimeMillis();
                        Float bpm = bb.getFloat();
                        Float uterus = bb.getFloat();
                        if (bpm == -1) bpm = null;
                        if (uterus == -1) uterus = null;

                        Sample sample = new Sample(ts, bpm, uterus, "patient-001");
                        sink.tryEmitNext(sample);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unix socket server failed", e);
        }
    }

    public Flux<Sample> flux() {
        return sink.asFlux();
    }

    public synchronized void startScanning() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            System.out.println("Python-скрипт уже запущен");
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "emulator.py");
            pb.redirectErrorStream(true); // объединяем stdout и stderr
            pythonProcess = pb.start();

//            logExecutor.submit(() -> logStream(pythonProcess.getInputStream()));

            System.out.println("Cканирование запущено (PID=" + pythonProcess.pid() + ")");
        } catch (Exception e) {
            throw new RuntimeException("Не удалось запустить сканирование", e);
        }
    }

    public synchronized void stopScanning() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
            System.out.println("Сканирование остановлено (PID=" + pythonProcess.pid() + ")");
            pythonProcess = null;
        }
    }

    private void logStream(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[" + "PYTHON" + "] " + line);
            }
        } catch (IOException ignored) {
        }
    }

    @PreDestroy
    public void stop() {
        if (serverFuture != null) serverFuture.cancel(true);
        if (executor != null) executor.shutdownNow();
        if (logExecutor != null) logExecutor.shutdownNow();
        stopScanning();
    }
}
