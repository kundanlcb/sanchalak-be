# Stage 1: Build the application
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /home/gradle/src

# Copy only necessary files for dependency resolution first to leverage caching
COPY --chown=gradle:gradle build.gradle settings.gradle gradlew ./
COPY --chown=gradle:gradle gradle ./gradle

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies to cache them in a separate layer
RUN ./gradlew dependencies --no-daemon || true

COPY --chown=gradle:gradle src ./src

# Build the application, skipping tests to speed up
RUN ./gradlew build --no-daemon -x test

# Stage 2: Create the runtime image
FROM eclipse-temurin:25-jre

WORKDIR /app

# Create a non-root user for security
RUN groupadd -r sanchalak && useradd -r -g sanchalak sanchalak
USER sanchalak:sanchalak

# Copy the built jar from the builder stage
COPY --from=builder /home/gradle/src/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Configure JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
