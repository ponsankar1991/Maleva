# Build and run with Docker

This project contains a multi-stage `Dockerfile` that builds the project using Maven and produces a runtime image.

Prerequisites:
- Docker installed and running
- (Optional) Maven and JDK if you want to build the WAR locally first

Build the Docker image (from project root):

```powershell
# Build image (Docker will run mvn package during the build stage). Replace `maleva-api` with your desired image name.
docker build -t maleva-api:latest .
```

Run the container exposing port 8080:

```powershell
# Run container, mapping host 8080 to container 8080
docker run --rm -p 8080:8080 --name maleva-api -e SPRING_REDIS_HOST=host.docker.internal maleva-api:latest
```

Notes:
- The Dockerfile packages the app into `/app/app.war` and runs it with `java -jar`.
- For Redis connectivity from Docker on Windows, use `host.docker.internal` as the Redis host (`SPRING_REDIS_HOST`) or run Redis as another container and configure a compose stack.
- To mount configuration or provide a different `application.yaml`, mount a file into `/app/config/` and pass `--spring.config.location=file:/app/config/application.yaml` to the java command (or set SPRING_APPLICATION_JSON environment variable).
