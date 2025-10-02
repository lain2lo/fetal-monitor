package io.github.lain2lo.ktgmonitor.api.dto;

import java.util.Map;

public record InferenceResultDto(String inferenceId, String patientId, Map<String,Double> risks, long createdAt) {}
