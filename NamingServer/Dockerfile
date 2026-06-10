FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
EXPOSE 8761
ADD target/naming-server-1.0.0.jar /app/naming-server.jar
ENTRYPOINT ["java", "-jar", "/app/naming-server.jar"]