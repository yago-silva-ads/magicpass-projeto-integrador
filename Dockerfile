# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:resolve
COPY backend .
RUN mvn -B -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/magicpass-1.0.0.jar .
EXPOSE $PORT
CMD ["java", "-jar", "magicpass-1.0.0.jar", "--server.port=${PORT:8080}"]
