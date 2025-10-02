import React, {useState} from "react";
import {BpmPredictionChart} from "./BpmPredictionChart";
import {Modal} from "./Modal";
import {useBpmPredictionSse} from "../hooks/useBpmPredictionSse";

export const BpmPredictionModal: React.FC = () => {
    const [isOpen, setIsOpen] = useState(false);

    // SSE включается только когда модалка открыта
    const prediction = useBpmPredictionSse(
        "http://localhost:8080/api/v1/sse/bpm-predict/latest",
        isOpen
    );

    return (
        <div>
            <button
                onClick={() => setIsOpen(true)}
                style={{
                    padding: "8px 16px",
                    fontSize: "16px",
                    backgroundColor: "#4caf50",
                    color: "#fff",
                    border: "none",
                    borderRadius: "4px",
                    cursor: "pointer"
                }}
            >
                Показать прогноз BPM
            </button>

            <Modal isOpen={isOpen} onClose={() => setIsOpen(false)}>
                <h2>Прогноз BPM</h2>
                {prediction ? (
                    <BpmPredictionChart result={prediction}/>
                ) : (
                    <p>Загрузка данных...</p>
                )}
            </Modal>
        </div>
    );
};