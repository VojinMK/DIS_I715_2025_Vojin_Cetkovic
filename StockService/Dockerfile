FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
EXPOSE 8772
ADD target/stock-service-1.0.0.jar /app/stock-service.jar
ENTRYPOINT ["java", "-jar", "/app/stock-service.jar"]