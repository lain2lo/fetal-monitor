# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle ./
COPY src/ src/
COPY emulator.py .
COPY model_v0_28.09.25.onnx .
COPY seq2seq_v0_30.09.25.onnx .

RUN chmod +x gradlew

RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update && apt-get install -y python3 python3-pip && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar
COPY emulator.py .
COPY model_v0_28.09.25.onnx .
COPY seq2seq_v0_30.09.25.onnx .
COPY bpm.csv .
COPY uterus.csv .

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]