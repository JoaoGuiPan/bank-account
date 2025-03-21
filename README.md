# Banking Account API

This project is an application that simulates common bank account operations.

## Prerequisites

Before you begin, ensure you have the following installed:

*   **Java Development Kit (JDK) 21:** Required to build and run the backend application.
*   **Gradle:** The backend build automation system.
*   **Node:** Required to build the frontend application.
*   **Angular CLI:** Required to develop and serve locally the frontend application.
*   **Docker:** To run the applications in a container.

## Running the Application

You can run the backend and frontend applications using either Gradle with Node or Docker.

### Running the backend with Gradle

1. **Build the Application:**

   if you are on a UNIX based system:
    ```bash
    ./banking-account-api/gradlew clean build
    ```

   or if you are on Windows:
   ```bash
    banking-account-api/gradlew.bat clean build
    ```

2. **Run the Application:**

   if you are on a UNIX based system:
    ```bash
    ./banking-account-api/gradlew bootRun
    ```

   or if you are on Windows:
   ```bash
    banking-account-api/gradlew.bat bootRun
    ```

### Access the Backend

Once it is running, you can access the application at `http://localhost:8080`.

### Swagger ui

You can access Swagger documentation at `http://localhost:8080/swagger-ui.html`.


### Running the frontend with Angular CLI

1. **Run the Application:**

    ```bash
    ng serve
    ```

### Access the Frontend

Once it is running, you can access the frontend at `http://localhost:4200`.


### Running with Docker

1. **Build the Docker Image:**

    ```bash
    docker compose build
    ```

2. **Run the Docker Container:**

    ```bash
    docker compose up
    ```

### Access the Application container

Once it is running, you can access the frontend at `http://localhost:8081` or the backend at `http://localhost:8080` with Swagger at `http://localhost:8080/swagger-ui.html`.
