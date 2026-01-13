# Multi-stage build: compile with Maven (JDK 17), then run on a slim JRE image.

# Build stage
FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -DskipTests clean package

# Runtime stage
FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/openrank-0.0.1-SNAPSHOT.jar app.jar

# Default port (can be overridden via SERVER_PORT env)
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
