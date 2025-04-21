# Base Image
FROM openjdk:17-jdk-slim

# Set Working Directory
WORKDIR /app

# Copy the JAR file from the build output
COPY target/websocket-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java" , "-jar" , "app.jar"]