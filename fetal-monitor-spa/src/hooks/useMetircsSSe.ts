import { useEffect, useState } from "react";
import type { KeyMetrics } from "../types";

export function useMetricsSse(url: string): KeyMetrics | null {
    const [metrics, setMetrics] = useState<KeyMetrics | null>(null);

    useEffect(() => {
        const es = new EventSource(url);

        es.onmessage = (e) => {
            try {
                const data: KeyMetrics = JSON.parse(e.data);
                setMetrics(data);
            } catch (err) {
                console.error("Failed to parse SSE data:", err);
            }
        };

        es.onerror = (err) => console.error("SSE error:", err);
        return () => es.close();
    }, [url]);

    return metrics;
}
