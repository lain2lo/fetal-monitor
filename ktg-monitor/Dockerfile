# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Копируем Gradle Wrapper и файлы проекта
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle ./
COPY src/ src/
COPY emulator.py .
COPY model_v0_28.09.25.onnx .
COPY seq2seq_v0_30.09.25.onnx .

# Делаем wrapper исполняемым
RUN chmod +x gradlew

# Собираем fat JAR через Gradle Wrapper
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем JAR и вспомогательные файлы
COPY --from=builder /app/build/libs/*.jar app.jar
COPY emulator.py .
COPY model_v0_28.09.25.onnx .
COPY seq2seq_v0_30.09.25.onnx .
COPY uterus.csv .
COPY bpm.csv .

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]