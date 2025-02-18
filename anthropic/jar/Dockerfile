# Use an official OpenJDK runtime as a base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the build stage
COPY /anthropic-0.0.1-SNAPSHOT.jar /app/anthropic.jar

# Expose the application port (change if needed)
EXPOSE 8080

# Run the Spring Boot app
CMD ["java", "-jar", "/app/anthropic.jar"]
