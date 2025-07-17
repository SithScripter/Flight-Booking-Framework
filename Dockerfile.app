# ✅ Base image with Maven and JDK 21
FROM maven:3.9-eclipse-temurin-21

# Set working directory
WORKDIR /usr/src/app

USER root

# ✅ Install curl
RUN apt-get update && apt-get install -y curl

# Optional: default CMD if ever used interactively
CMD ["mvn", "--version"]
