# Sanchalak Backend (Next-Gen)

This repository contains the backend API for Sanchalak School Management System, updated to the latest bleeding-edge stack.

## Technology Stack

*   **Java**: 25 (Eclipse Temurin)
*   **Framework**: Spring Boot 4.0.2-SNAPSHOT
*   **Build Tool**: Gradle 9.3
*   **Database**: MySQL 8.0+
*   **Containerization**: Docker & Docker Compose

## Quick Start (Docker)

The easiest way to run the application is using Docker.

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/kundanlcb/sanchalak-be.git
    cd sanchalak-be
    ```

2.  **Configure Environment**:
    *   Edit `.env` if you need to change database credentials or secrets.
    *   Default DB password: `Sanghi@12345` (matches `application.yaml`).

3.  **Run with Docker Compose**:
    ```bash
    docker-compose up -d --build
    ```

4.  **Access the Application**:
    *   **Health Check**: [http://localhost:8082/ping](http://localhost:8082/ping)
    *   **Swagger UI**: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
    *   **API Docs**: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

## Local Development

To run locally, you must have **Java 25** installed.

1.  **Install Java 25**: Ensure `JAVA_HOME` points to JDK 25.
2.  **Run Application**:
    ```bash
    ./gradlew bootRun
    ```
    *Note: Integration tests are currently excluded from the build due to Spring Boot 4 test context changes.*

## Configuration

*   **Database**: `jdbc:mysql://host.docker.internal:3306/sanchalak` (Docker) or `localhost:3306` (Local).
*   **Security**: JWT Authentication + Public `/ping` endpoint.

## Mobile API Features

*   **Authentication**: OTP-based login.
*   **Notifications**: Firebase Cloud Messaging (FCM).
*   **Storage**: Azure Blob Storage / AWS S3 support.
