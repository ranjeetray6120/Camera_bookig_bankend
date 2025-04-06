# -----------------------------
# Stage 1: Build the project using Maven
# -----------------------------
FROM maven:3.8.1-openjdk-17 AS build

# Set working directory inside the container
WORKDIR /app

# Copy all project files into the container
COPY . .

# Build the project and skip tests
RUN mvn clean package -DskipTests

# -----------------------------
# Stage 2: Run the app
# -----------------------------
FROM openjdk:17-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
