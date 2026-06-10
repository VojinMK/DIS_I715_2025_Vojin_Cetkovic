FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
EXPOSE 8770
ADD target/user-service-1.0.0.jar /app/user-service.jar
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]