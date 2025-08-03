## Running the Project with Docker

This project includes a multi-stage Docker setup for building and running a Java 17 (Spring Boot) application using Maven. The provided `Dockerfile` and `docker-compose.yml` files streamline the build and deployment process.

### Requirements
- **Java Version:** 17 (Eclipse Temurin base images)
- **Build Tool:** Maven Wrapper (`mvnw`)
- **No external services** (e.g., databases) are required by default.

### Environment Variables
- No required environment variables are specified in the Dockerfile or compose file by default.
- If you need to provide custom environment variables, you can uncomment and use the `env_file: ./.env` line in `docker-compose.yml`.

### Build and Run Instructions
1. **Build and start the application:**
   ```sh
   docker compose up --build
   ```
   This will build the application using the Maven wrapper and run it in a container as a non-root user.

2. **Access the application:**
   - The application will be available on [http://localhost:8080](http://localhost:8080) by default.

### Configuration Notes
- The application runs as a non-root user (`appuser`) for improved security.
- JVM is configured for container environments with `JAVA_OPTS` set to use up to 80% of available RAM.
- If you need to override JVM options, set the `JAVA_OPTS` environment variable in your compose file or `.env` file.

### Exposed Ports
- **8080:** The default Spring Boot port is exposed and mapped to the host.

---

*If you add external services (like a database), update the `docker-compose.yml` accordingly and document any new environment variables or ports required.*
