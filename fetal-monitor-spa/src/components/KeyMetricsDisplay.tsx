import React from "react";
import {type KeyMetrics, type MetricValue, type Status, StatusLabels, TrendLabels} from "../types";

interface Props {
    metrics: KeyMetrics;
}

// Переводы метрик
const metricLabels: Record<string, string> = {
    baseline: "Базальная ЧСС",
    variability: "Вариабельность",
    STV: "Кратковременные вариации (STV)",
    LTV: "Долговременные вариации (LTV)",
    accelerations: "Акцелерации",
    decelerations: "Децелерации",
    uterineActivity: "Маточная активность",
    baselineStability: "Стабильность базальной линии",
};

// Группировка с переводом
const metricGroups: Record<string, string[]> = {
    "ЧСС плода": ["baseline", "variability", "STV", "LTV", "accelerations", "decelerations"],
    "Матка": ["uterineActivity", "baselineStability"],
    // "Индексы": [], // отдельно обработаем
};

const getColor = (status: Status | undefined): string => {
    switch (status) {
        case "NORMAL":
            return "#00cc00";
        case "SUSPICIOUS":
            return "#ff9900";
        case "PATHOLOGICAL":
            return "#ff0000";
        default:
            return "#999999";
    }
};
// Определение цвета заголовка группы
const getGroupStatusColor = (metrics: (MetricValue | undefined)[]): string => {
    if (metrics.some((m) => m?.status === "PATHOLOGICAL")) return "#ff0000";
    if (metrics.some((m) => m?.status === "SUSPICIOUS")) return "#ff9900";
    if (metrics.some((m) => m?.status === "NORMAL")) return "#00cc00";
    return "#666";
};

export const KeyMetricsDisplay: React.FC<Props> = ({metrics}) => {
    const renderMetricRow = (key: string, metric?: MetricValue | undefined) => {
        if (!metric) {
            return (
                <div key={key} style={{display: "flex", alignItems: "center", marginBottom: 12, gap: 12}}>
                    <div
                        style={{width: 14, height: 14, borderRadius: "50%", background: "#ddd"}}
                        title="Нет данных"
                    />
                    <div style={{color: "#666"}}>
                        <strong>{metricLabels[key] ?? key}:</strong> — (нет данных)
                    </div>
                </div>
            );
        }

        const color = getColor(metric.status);
        const valText = metric.value === null || metric.value === undefined ? "-" : metric.value.toFixed(1);

        return (
            <div key={key} style={{display: "flex", alignItems: "center", marginBottom: 12, gap: 12}}>
                {/* Круглый индикатор с tooltip */}
                <div
                    style={{
                        width: 14,
                        height: 14,
                        borderRadius: "50%",
                        backgroundColor: color,
                        flexShrink: 0,
                        cursor: "help",
                    }}
                    title={`Состояние: ${metric.status}`}
                />

                {/* Название + значение */}
                <div style={{display: "flex", flexDirection: "column"}}>
                    <div style={{display: "flex", alignItems: "center", gap: 8}}>
                        <strong>{metricLabels[key] ?? key}:</strong>
                        <span style={{fontSize: 18, fontWeight: 600}}>{valText}</span>
                        <span style={{marginLeft: 6, color: "#444", fontSize: 14}}>
              ({StatusLabels[metric.status]}, {TrendLabels[metric.trend]})
            </span>
                    </div>
                </div>
            </div>
        );
    };

    return (
        <div
            style={{
                fontSize: 18,
                flex: "1 1 0%",
                background: "rgb(249, 249, 249)",
                padding: 20,
                display: "flex",
                flexDirection: "column",
                alignItems: "flex-start",
                borderLeft: "1px solid rgb(221, 221, 221)",
                gap: 6,
            }}
        >
            <h2 style={{alignSelf: "center", margin: "0 0 12px 0"}}>Ключевые показатели</h2>

            {/* Группы метрик */}
            {Object.entries(metricGroups).map(([groupName, keys]) => {
                const groupMetrics = keys.map((k) => metrics.metrics[k]);
                const groupColor = getGroupStatusColor(groupMetrics);

                return (
                    <div key={groupName} style={{marginBottom: 16, width: "100%"}}>
                        <h3 style={{marginBottom: 8, color: groupColor}}>{groupName}</h3>
                        {keys.map((k) => renderMetricRow(k, metrics.metrics[k]))}
                    </div>
                );
            })}

            {/* Прогностические индексы */}
            <div style={{marginTop: 12, borderTop: "1px solid #eee", paddingTop: 12, width: "100%"}}>
                <h3 style={{marginBottom: 8, color: "#333"}}>Индексы</h3>
                <div style={{display: "flex", gap: 16, alignItems: "center"}}>
                    <div>
                        <strong>Шкала Фишера:</strong>{" "}
                        <span style={{fontWeight: 600}}>
              {metrics.prognosticScores?.Fisher ?? "-"}
            </span>
                    </div>
                    <div>
                        <strong>Шкала Кребса:</strong>{" "}
                        <span style={{fontWeight: 600}}>
              {metrics.prognosticScores?.Krebs ?? "-"}
            </span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default KeyMetricsDisplay;
