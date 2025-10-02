import React, {useEffect, useState} from "react";
import {Graph} from "./components/Graph";
import {KeyMetricsDisplay} from "./components/KeyMetricsDisplay";
import type {Sample} from "./types";
import {useMetricsSse} from "./hooks/useMetircsSSe";
import {useInferenceSse} from "./hooks/UseInferenceSse.ts";
import {DriverControls} from "./components/DriverControls";
import {InferenceResultCard} from "./components/InferenceResultCard";
import {BpmPredictionModal} from "./components/BpmPredictionModal.tsx";

const baseUrl: string = "http://localhost:8080";

const App: React.FC = () => {
    const [samples, setSamples] = useState<Sample[]>([]);
    const metrics = useMetricsSse(baseUrl + "/api/v1/sse/metrics");
    const inference = useInferenceSse(baseUrl + "/api/v1/sse/inference/stream"); // ⚡ новый хук

    useEffect(() => {
        const ws = new WebSocket("ws://localhost:8080/api/v1/ws/samples");
        ws.onmessage = (e) => {
            const sample: Sample = JSON.parse(e.data);
            setSamples((prev) => [...prev.slice(-5000), sample]);
        };
        return () => ws.close();
    }, []);

    return (
        <div
            style={{
                width: "1920px",
                height: "1080px",
                display: "flex",
                flexDirection: "column",
                background: "#fff",
            }}
        >
            <header
                style={{
                    flex: "0 0 60px",
                    padding: "10px 20px",
                    background: "#f5f5f5",
                    borderBottom: "1px solid #ddd",
                }}
            >
                <div style={{padding: "10px"}}>
                    <DriverControls/>
                </div>
            </header>

            <div style={{flex: 1, display: "flex"}}>
                <div
                    style={{
                        flex: 2,
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "space-around",
                        padding: "10px",
                        gap: "10px",
                    }}
                >
                    <div style={{marginTop: "10px"}}>
                        <BpmPredictionModal/>
                    </div>
                    <Graph chartType="bpm" samples={samples}/>
                    <Graph chartType="uterus" samples={samples}/>
                </div>

                <div
                    style={{
                        flex: 1,
                        background: "#f9f9f9",
                        padding: "20px",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "stretch",
                        gap: "20px",
                        borderLeft: "1px solid #ddd",
                    }}
                >
                    {metrics ? <KeyMetricsDisplay metrics={metrics}/> : "Загрузка метрик... \n"}

                    {inference ? (
                        <InferenceResultCard result={inference}/>
                    ) : (
                        "Жду результатов инференса..."
                    )}

                </div>
            </div>
        </div>
    );
};

export default App;
