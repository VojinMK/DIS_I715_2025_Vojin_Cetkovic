FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
EXPOSE 8888
ADD target/config-server-1.0.0.jar /app/config-server.jar
ENTRYPOINT ["java", "-jar", "/app/config-server.jar"]