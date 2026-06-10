FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
EXPOSE 8771
ADD target/product-service-1.0.0.jar /app/product-service.jar
ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]