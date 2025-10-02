package io.github.lain2lo.ktgmonitor.infrastructure.ml;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import io.github.lain2lo.ktgmonitor.domain.InferenceResult;
import io.github.lain2lo.ktgmonitor.domain.Sample;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

public class ONNXInferenceHypoxiaEngine {

    private final OrtEnvironment env;
    private final OrtSession session;

    public ONNXInferenceHypoxiaEngine(String modelPath) throws OrtException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(modelPath, new OrtSession.SessionOptions());
    }

    public InferenceResult run(String patientId, List<Sample> window) {
        try {
            // Отбрасываем null
            List<Sample> filteredWindow = window.stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (filteredWindow.isEmpty()) {
                throw new IllegalArgumentException("No valid samples to run inference");
            }

            int seqLen = filteredWindow.size();

            // 1 batch, 1 channel, seqLen
            float[][][] modelInput = new float[1][1][seqLen];

            // нормализация
            double mean = filteredWindow.stream().mapToDouble(Sample::bpm).average().orElse(0.0);
            double std = Math.sqrt(filteredWindow.stream().mapToDouble(s -> Math.pow(s.bpm() - mean, 2)).average().orElse(1e-6));

            for (int i = 0; i < seqLen; i++) {
                modelInput[0][0][i] = (float) ((filteredWindow.get(i).bpm() - mean) / (std + 1e-6));
            }

            Map<String, OnnxTensor> inputs = Map.of(
                    "input", OnnxTensor.createTensor(env, modelInput)
            );

            OrtSession.Result results = session.run(inputs);

            float[][] logits = (float[][]) results.get(0).getValue();

            INDArray logitsArr = Nd4j.createFromArray(logits[0]);
            INDArray probsArr = Nd4j.nn.softmax(logitsArr, 0);

            Map<String, Double> risks = new HashMap<>();
            risks.put("normal", probsArr.getDouble(0));
            risks.put("hypoxia", probsArr.getDouble(1));

            return new InferenceResult(
                    UUID.randomUUID().toString(),
                    patientId,
                    risks,
                    filteredWindow,  // сохраняем только валидные Sample
                    System.currentTimeMillis()
            );

        } catch (Exception e) {
            throw new RuntimeException("ONNX inference failed", e);
        }
    }
}
