FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
EXPOSE 9005
ADD target/email-service-1.0.0.jar /app/email-service.jar
ENTRYPOINT ["java", "-jar", "/app/email-service.jar"]