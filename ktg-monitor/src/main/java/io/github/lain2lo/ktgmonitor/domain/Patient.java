package io.github.lain2lo.ktgmonitor.domain;

public record Patient(
        String patientId,
        String name,
        String medicalRecordId
) {}
