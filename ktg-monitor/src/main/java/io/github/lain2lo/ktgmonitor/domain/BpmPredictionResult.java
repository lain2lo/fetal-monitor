package io.github.lain2lo.ktgmonitor.domain;

import java.util.List;

public class BpmPredictionResult {

    private String predictionId;
    private List<PredictedSample> predictedValues;
    private List<Sample> samples;
    private long createdAt;

    public BpmPredictionResult() {

    }

    public BpmPredictionResult(String predictionId, List<PredictedSample> predictedValues, long createdAt, List<Sample> samples) {
        this.predictionId = predictionId;
        this.predictedValues = predictedValues;
        this.createdAt = createdAt;
        this.samples = samples;
    }

    public String getPredictionId() {
        return predictionId;
    }

    public void setPredictionId(String predictionId) {
        this.predictionId = predictionId;
    }

    public List<PredictedSample> getPredictedValues() {
        return predictedValues;
    }

    public void setPredictedValues(List<PredictedSample> predictedValues) {
        this.predictedValues = predictedValues;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public static class PredictedSample {
        private long time;
        private Double bpm;

        public PredictedSample() {
        }

        public PredictedSample(long time, Double bpm) {
            this.time = time;
            this.bpm = bpm;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public Double getBpm() {
            return bpm;
        }

        public void setBpm(Double bpm) {
            this.bpm = bpm;
        }
    }
}
