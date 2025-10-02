import { useEffect, useState } from "react";
import type { BpmPredictionResult } from "../types";

export const useBpmPredictionSse = (sseUrl: string, enabled: boolean) => {
    const [result, setResult] = useState<BpmPredictionResult | null>(null);

    useEffect(() => {
        if (!enabled) return; // соединение не открываем

        const evtSource = new EventSource(sseUrl);

        evtSource.onmessage = (event) => {
            const data: BpmPredictionResult = JSON.parse(event.data);
            setResult(data);
        };

        return () => evtSource.close(); // закрываем при выключении
    }, [sseUrl, enabled]);

    return result;
};
