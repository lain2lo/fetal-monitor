import {Line} from "react-chartjs-2";
import zoomPlugin from "chartjs-plugin-zoom";
import {
    CategoryScale, Chart as ChartJS,
    type ChartOptions,
    Legend,
    LinearScale,
    LineElement,
    PointElement,
    TimeScale,
    Title,
    Tooltip
} from "chart.js";
import annotationPlugin from "chartjs-plugin-annotation";
import 'chartjs-adapter-date-fns';
import {ranges, type Sample} from "../types";

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    TimeScale,
    annotationPlugin,
    zoomPlugin
);

interface GraphProps {
    chartType: "bpm" | "uterus";
    samples: Sample[];
}

export const Graph: React.FC<GraphProps> = ({chartType, samples}) => {

    const data = {
        datasets: [
            {
                label: chartType === "bpm" ? "BPM" : "Uterus",
                data: samples.map((s: Sample) => ({
                    x: new Date(s.timeSec), // корректная дата
                    // x: s.tsMs, // корректная дата
                    y: chartType === "bpm" ? s.bpm : s.uterus,
                })),
                borderColor: chartType === "bpm" ? "rgb(75, 192, 192)" : "rgb(255, 99, 132)",
                tension: 0,
                pointRadius: 0,
                spanGaps: false,
            },
        ],
    };

    const opts: ChartOptions<"line"> = {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        scales: {
            y: {
                min: ranges[chartType].min,
                max: ranges[chartType].max,
                ticks: {stepSize: chartType === "bpm" ? 10 : 5, font: {size: 18}},
                title: {display: true, text: chartType === "bpm" ? "BPM" : "Uterus mmHg", font: {size: 18}},
            },
            x: {
                type: "time",
                time: {
                    unit: "second",
                    tooltipFormat: "HH:mm:ss",
                    displayFormats: {second: "HH:mm:ss"},
                },
                title: {display: false},
            },
        },
        plugins: {
            legend: {display: false},
            annotation: {
                annotations: {
                    safeZone: {
                        type: "box",
                        yMin: ranges[chartType].safeMin,
                        yMax: ranges[chartType].safeMax,
                        backgroundColor: "rgba(0, 255, 0, 0.1)",
                    },
                    warningLow: {
                        type: "box",
                        yMin: ranges[chartType].warnMin,
                        yMax: ranges[chartType].safeMin,
                        backgroundColor: "rgba(255, 255, 0, 0.1)",
                    },
                    warningHigh: {
                        type: "box",
                        yMin: ranges[chartType].safeMax,
                        yMax: ranges[chartType].warnMax,
                        backgroundColor: "rgba(255, 255, 0, 0.1)",
                    },
                    dangerLow: {
                        type: "box",
                        yMin: ranges[chartType].min,
                        yMax: ranges[chartType].warnMin,
                        backgroundColor: "rgba(255, 0, 0, 0.1)",
                    },
                    dangerHigh: {
                        type: "box",
                        yMin: ranges[chartType].warnMax,
                        yMax: ranges[chartType].max,
                        backgroundColor: "rgba(255, 0, 0, 0.1)",
                    },
                },
            },
            zoom: {
                pan: {
                    enabled: true,
                    mode: "x", // можно двигать только по оси X
                },
                zoom: {
                    wheel: {
                        enabled: true, // зум колесиком мыши
                    },
                    pinch: {
                        enabled: true, // зум жестами
                    },
                    mode: "x", // масштабирование по X
                },
                limits: {
                    x: {
                        min: Date.now() - 60 * 60_000, // нельзя уйти больше чем на 1 час назад
                        max: Date.now(),               // нельзя уйти в будущее
                    },
                },
            },
        },
    };

    // const chartRef = useRef<Chart<"line", { x: Date; y: number }[], unknown> | null>(null);
    return (
        <div style={{width: "100%", height: "500px"}}>
            <Line data={data} options={opts}/>
        </div>
    );
};
