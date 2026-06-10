FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
EXPOSE 8773
ADD target/order-service-1.0.0.jar /app/order-service.jar
ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]