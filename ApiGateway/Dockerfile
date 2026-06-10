FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
EXPOSE 8765
ADD target/api-gateway-1.0.0.jar /app/api-gateway.jar
ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]