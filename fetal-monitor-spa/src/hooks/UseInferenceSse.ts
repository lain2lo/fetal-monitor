// hooks/useInferenceSse.ts
import { useEffect, useState } from "react";
import type { InferenceResult } from "../components/InferenceResultCard";

export function useInferenceSse(url: string) {
    const [result, setResult] = useState<InferenceResult | null>(null);

    useEffect(() => {
        const es = new EventSource(url);

        es.onmessage = (event) => {
            try {
                const data: InferenceResult = JSON.parse(event.data);
                setResult(data);
            } catch (err) {
                console.error("Ошибка парсинга инференса:", err);
            }
        };

        es.onerror = (err) => {
            console.error("SSE ошибка:", err);
            es.close();
        };

        return () => es.close();
    }, [url]);

    return result;
}
