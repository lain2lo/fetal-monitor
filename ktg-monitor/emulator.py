import csv
import struct
import socket
import time
from bisect import bisect_left

SOCKET_PATH = "/tmp/ktg.sock"
MAX_DIFF = 0.05  # максимально допустимая разница во времени для синхронизации, сек
MISSING_VALUE = -1.0  # значение для отсутствующих данных

class Sample:
    def __init__(self, ts_sec, bpm, uterus):
        self.ts_sec = ts_sec
        self.bpm = bpm
        self.uterus = uterus

def read_csv(path):
    data = []
    with open(path, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            ts = float(row['time_sec'])
            value = float(row['value'])
            data.append((ts, value))
    return data

def synchronize_data(bpm_data, uterus_data, max_diff=MAX_DIFF):
    """Сопоставляем bpm и uterus по временным меткам"""
    samples = []
    uterus_times = [ts for ts, _ in uterus_data]
    for ts_bpm, bpm in bpm_data:
        idx = bisect_left(uterus_times, ts_bpm)
        uterus_val = MISSING_VALUE  # по умолчанию отсутствующее значение
        candidates = []
        if idx < len(uterus_data):
            candidates.append(uterus_data[idx])
        if idx > 0:
            candidates.append(uterus_data[idx - 1])
        if candidates:
            ts_uterus, uterus = min(candidates, key=lambda x: abs(x[0] - ts_bpm))
            if abs(ts_uterus - ts_bpm) <= max_diff:
                uterus_val = uterus
        samples.append(Sample(ts_bpm, bpm, uterus_val))
    return samples

def send_samples_with_delay(samples):
    """Отправляем данные с задержкой, имитируя устройство"""
    sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    sock.connect(SOCKET_PATH)

    if not samples:
        return

    prev_ts = samples[0].ts_sec
    for s in samples:
        # ждем интервал между текущим и предыдущим сэмплом
        wait = s.ts_sec - prev_ts
        if wait > 0:
            time.sleep(wait)
        prev_ts = s.ts_sec

        # формируем пакет: bpm и uterus, при отсутствии = -1.0
        buf = struct.pack('<ff', s.bpm if s.bpm is not None else MISSING_VALUE,
                          s.uterus if s.uterus is not None else MISSING_VALUE)
        sock.sendall(buf)
        print(f"Sent: bpm={s.bpm}, uterus={s.uterus}")

    sock.close()

if __name__ == "__main__":
    bpm_data = read_csv("/app/bpm.csv")
    uterus_data = read_csv("/app/uterus.csv")
    samples = synchronize_data(bpm_data, uterus_data)
    send_samples_with_delay(samples)
