import React, { useState } from "react";

interface Props {
    baseUrl?: string;
}

export const DriverControls: React.FC<Props> = ({ baseUrl = "http://localhost:8080" }) => {
    const [status, setStatus] = useState<"idle" | "running" | "error">("idle");
    const [message, setMessage] = useState<string>("Остановлен");

    const sendCommand = async (command: "start" | "stop") => {
        try {
            const resp = await fetch(`${baseUrl}/api/v1/driver/${command}`, { method: "POST" });
            if (!resp.ok) throw new Error(`Ошибка: ${resp.status}`);
            const newStatus = command === "start" ? "running" : "idle";
            setStatus(newStatus);
            setMessage(command === "start" ? "Работает" : "Остановлен");
        } catch (err: unknown) {
            console.error(err);
            setStatus("error");
            setMessage("Ошибка");
        }
    };

    const getStatusColor = () => {
        switch (status) {
            case "running":
                return "#4CAF50"; // зелёный
            case "idle":
                return "#f44336"; // красный
            case "error":
                return "#FF9800"; // оранжевый
            default:
                return "#333";
        }
    };

    return (
        <div
            style={{
                display: "flex",
                alignItems: "center",
                gap: "12px",
                padding: "8px 12px",
                background: "#f9f9f9",
                border: "1px solid #ddd",
                borderRadius: "8px",
                fontSize: "14px",
            }}
        >
            <button
                onClick={() => sendCommand("start")}
                disabled={status === "running"}
                style={{
                    padding: "6px 12px",
                    background: status === "running" ? "#ccc" : "#4CAF50",
                    color: "#fff",
                    border: "none",
                    borderRadius: "6px",
                    cursor: status === "running" ? "not-allowed" : "pointer",
                }}
            >
                ▶ Старт
            </button>
            <button
                onClick={() => sendCommand("stop")}
                disabled={status === "idle"}
                style={{
                    padding: "6px 12px",
                    background: status === "idle" ? "#ccc" : "#f44336",
                    color: "#fff",
                    border: "none",
                    borderRadius: "6px",
                    cursor: status === "idle" ? "not-allowed" : "pointer",
                }}
            >
                ⏹ Стоп
            </button>
            <div style={{ display: "flex", alignItems: "center", marginLeft: "auto", gap: 6 }}>
                    {/* Индикатор состояния */}
                <span
                    style={{
                        width: 14,
                        height: 14,
                        borderRadius: "50%",
                        backgroundColor: getStatusColor(),
                        display: "inline-block",
                    }}
                />
                <span style={{ fontWeight: 600 }}>{message}</span>
            </div>
        </div>
    );
};
