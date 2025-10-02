package io.github.lain2lo.ktgmonitor.domain;

public record Sample(
        long timeSec,
        Float bpm,
        Float uterus,
        String patientId
) {}
