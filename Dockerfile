FROM eclipse-temurin:17-jdk-alpine

ARG JAR_FILE=target/camerabooking-0.0.1-SNAPSHOT.jar

COPY  app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
