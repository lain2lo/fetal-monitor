package io.github.lain2lo.ktgmonitor.infrastructure.ml;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import io.github.lain2lo.ktgmonitor.domain.BpmPredictionResult;
import io.github.lain2lo.ktgmonitor.domain.Sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ONNXInferenceBpmPredictEngine {

    private final OrtEnvironment env;
    private final OrtSession session;
    private final String inputName;

    public ONNXInferenceBpmPredictEngine(String modelPath) throws OrtException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(modelPath, new OrtSession.SessionOptions());
        inputName = session.getInputNames().iterator().next();
    }

    public BpmPredictionResult run(List<Sample> window) {
        try {
            List<Sample> filteredWindow = window.stream()
                    .filter(s -> s.bpm() != null)
                    .toList();

            if (filteredWindow.isEmpty()) {
                throw new IllegalArgumentException("No valid BPM samples for prediction");
            }

            // длина входного окна для модели
            int INPUT_LEN = 1000;
            int startIdx = Math.max(0, filteredWindow.size() - INPUT_LEN);
            float[] seqWindow = new float[INPUT_LEN];
            for (int i = 0; i < INPUT_LEN; i++) {
                if (startIdx + i < filteredWindow.size()) {
                    seqWindow[i] = filteredWindow.get(startIdx + i).bpm();
                } else {
                    seqWindow[i] = 0f;
                }
            }

            List<Double> predicted = new ArrayList<>();
            // количество шагов прогноза
            int PRED_STEPS = 1000;
            for (int step = 0; step < PRED_STEPS; step++) {
                OnnxTensor inputTensor = OnnxTensor.createTensor(env, new float[][]{seqWindow});
                try (OrtSession.Result result = session.run(Collections.singletonMap(inputName, inputTensor))) {
                    float[][] yHat = (float[][]) result.get(0).getValue();
                    float nextVal = yHat[0][0];
                    predicted.add((double) nextVal);

                    System.arraycopy(seqWindow, 1, seqWindow, 0, seqWindow.length - 1);
                    seqWindow[seqWindow.length - 1] = nextVal;
                }
                inputTensor.close();
            }

            // Генерируем временные метки для предсказаний
            long now = System.currentTimeMillis();
            long intervalMs = 1000; // 1 секунда между предсказаниями, можно настроить
            List<BpmPredictionResult.PredictedSample> predictedSamples = new ArrayList<>();
            for (int i = 0; i < predicted.size(); i++) {
                predictedSamples.add(new BpmPredictionResult.PredictedSample(now + i * intervalMs, predicted.get(i)));
            }

            return new BpmPredictionResult(
                    UUID.randomUUID().toString(),
                    predictedSamples,
                    now,
                    window
            );

        } catch (Exception e) {
            throw new RuntimeException("ONNX BPM prediction failed", e);
        }
    }
}