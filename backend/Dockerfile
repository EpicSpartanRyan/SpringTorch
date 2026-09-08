FROM gradle:9.7.1-jdk21 AS builder

WORKDIR /app

COPY . .

RUN gradle bootJar --no-daemon

FROM ubuntu:24.04

RUN apt-get update \
	&& apt-get install -y --no-install-recommends openjdk-21-jdk \
	&& rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
