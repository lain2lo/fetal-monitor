package io.github.lain2lo.ktgmonitor.domain;

import java.util.HashMap;
import java.util.Map;

public class KeyMetrics {
    private Map<String, MetricValue> metrics = new HashMap<>();
    private Map<String, Integer> prognosticScores = new HashMap<>();

    public KeyMetrics() {
    }

    public void addMetric(String name, Double value, String status, String trend) {
        metrics.put(name, new MetricValue(value, status, trend));
    }

    public void setPrognosticScore(String name, int score) {
        prognosticScores.put(name, score);
    }

    public Map<String, MetricValue> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, MetricValue> metrics) {
        this.metrics = metrics;
    }

    public Map<String, Integer> getPrognosticScores() {
        return prognosticScores;
    }

    public void setPrognosticScores(Map<String, Integer> prognosticScores) {
        this.prognosticScores = prognosticScores;
    }

    // 🔹 обычный класс вместо record
    public static class MetricValue {
        private Double value;
        private String status;
        private String trend;

        public MetricValue() {
        }

        public MetricValue(Double value, String status, String trend) {
            this.value = value;
            this.status = status;
            this.trend = trend;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTrend() {
            return trend;
        }

        public void setTrend(String trend) {
            this.trend = trend;
        }
    }
}
