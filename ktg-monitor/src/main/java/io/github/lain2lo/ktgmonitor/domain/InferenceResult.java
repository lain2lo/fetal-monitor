package io.github.lain2lo.ktgmonitor.domain;

import java.util.List;
import java.util.Map;

public record InferenceResult(
        String inferenceId,
        String patientId,
        Map<String, Double> risks,
        List<Sample> window,
        long createdAt
) {}
