import React from "react";
import { Line } from "react-chartjs-2";
import type { BpmPredictionResult, Sample } from "../types";

interface BpmPredictionChartProps {
    result: BpmPredictionResult;
}

export const BpmPredictionChart: React.FC<BpmPredictionChartProps> = ({ result }) => {
    const currentSamples = result.samples || [];
    const predictedBpm = result.predictedValues.map(p => ({ x: new Date(p.time), y: p.bpm }));

    const data = {
        datasets: [
            {
                label: "Текущий BPM",
                data: currentSamples.map((s: Sample) => ({ x: new Date(s.timeSec), y: s.bpm })),
                borderColor: "rgb(75, 192, 192)",
                tension: 0.2,
                spanGaps: true,
            },
            {
                label: "Прогноз BPM",
                data: predictedBpm,
                borderColor: "rgb(255, 99, 132)",
                borderDash: [5, 5],
                tension: 0.2,
                spanGaps: true,
            }
        ]
    };

    const options = {
        responsive: true,
        scales: {
            x: {
                type: "time" as const,
                time: {
                    unit: "second" as const,
                    tooltipFormat: "HH:mm:ss",
                    displayFormats: { second: "HH:mm:ss" }
                },
            },
            y: {
                min: 50,
                max: 210,
                title: { display: true, text: "BPM" }
            }
        }
    };

    return <Line data={data} options={options} />;
};
