export interface Sample {
    timeSec: number; // миллисекунды
    bpm: number;
    uterus: number;
}

export type Trend = "UP" | "DOWN" | "STABLE";
export type Status = "NORMAL" | "SUSPICIOUS" | "PATHOLOGICAL";

export const TrendLabels: Record<Trend, string> = {
    UP: "Рост",
    DOWN: "Падение",
    STABLE: "Стабильно",
};

export const StatusLabels: Record<Status, string> = {
    NORMAL: "Норма",
    SUSPICIOUS: "Подозрительно",
    PATHOLOGICAL: "Патологично",
};

export interface MetricValue {
    value: number | null;
    status: Status;
    trend: Trend;
}

export interface PrognosticScores {
    Fisher?: number;
    Krebs?: number;
}

export interface KeyMetrics {
    metrics: {
        baseline?: MetricValue;
        variability?: MetricValue;
        accelerations?: MetricValue;
        decelerations?: MetricValue;
        uterineActivity?: MetricValue;
        baselineStability?: MetricValue;
        STV?: MetricValue;
        LTV?: MetricValue;
        [key: string]: MetricValue | undefined; // для будущих метрик
    };
    prognosticScores?: PrognosticScores;
}
export const ranges = {
    bpm: {min: 80, max: 210, safeMin: 110, safeMax: 160, warnMin: 100, warnMax: 170},
    uterus: {min: 0, max: 60, safeMin: 10, safeMax: 50, warnMin: 5, warnMax: 55},
};

export interface BpmPredictionResult {
    predictionId: string;
    createdAt: number;
    samples: Sample[];
    predictedValues: {
        time: number;
        bpm: number;
    }[];
}