import React from "react";
import type {Sample} from "../types";

export interface InferenceResult {
    inferenceId: string;
    patientId: string;
    risks: Record<string, number>;
    window: Sample[];
    createdAt: number;
}

interface Props {
    result: InferenceResult;
}

const riskLabels: Record<string, string> = {
    normal: "Норма",
    hypoxia: "Гипоксия",
};

export const InferenceResultCard: React.FC<Props> = ({result}) => {
    const {inferenceId, risks, window, createdAt} = result;

    const start = window.length > 0 ? new Date(window[0].timeSec).toLocaleTimeString() : "-";
    const end = window.length > 0 ? new Date(window[window.length - 1].timeSec).toLocaleTimeString() : "-";
    const created = new Date(createdAt).toLocaleTimeString();

    return (
        <div
            style={{
                padding: "16px",
                border: "1px solid #ddd",
                borderRadius: "12px",
                background: "#fafafa",
                marginBottom: "12px",
            }}
        >
            <h3 style={{marginBottom: "8px"}}>Результат инференса</h3>
            <div style={{marginBottom: "6px", fontSize: "14px", color: "#666"}}>
                <strong>ID:</strong> {inferenceId}
            </div>
            <div style={{marginBottom: "6px", fontSize: "14px"}}>
                <strong>Окно данных:</strong> {start} – {end}
            </div>
            <div style={{marginBottom: "6px", fontSize: "14px"}}>
                <strong>Время расчёта:</strong> {created}
            </div>

            <div style={{marginTop: "10px"}}>
                <strong>Риски:</strong>
                <div style={{display: "flex", gap: "12px", marginTop: "6px"}}>
                    {Object.entries(risks).map(([key, value]) => {
                        const percent = Math.round(value * 100);
                        const label = riskLabels[key] ?? key;
                        const color =
                            key === "normal" ? "green" : key === "hypoxia" ? "red" : "gray";

                        return (
                            <div
                                key={key}
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    gap: "6px",
                                    fontSize: "14px",
                                }}
                            >
                <span
                    style={{
                        display: "inline-block",
                        width: "12px",
                        height: "12px",
                        borderRadius: "50%",
                        background: color,
                    }}
                />
                                <span>
                  {label}: <strong>{percent}%</strong>
                </span>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
};