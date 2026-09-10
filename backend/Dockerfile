FROM gradle:9.7.1-jdk21-resolute AS builder
# sdf ubuntu 26.04
WORKDIR /app

COPY . .

RUN gradle bootJar --no-daemon

FROM gradle:9.7.1-jdk21-resolute

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
