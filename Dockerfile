# ✅ Base image with Maven and JDK 21 (Temurin distribution)
FROM maven:3.9-eclipse-temurin-21

# ✅ Set working directory
WORKDIR /usr/src/app

# ✅ Install curl (required for Qase integration)
RUN apt-get update && apt-get install -y curl

# ✅ Default command (optional, Jenkins overrides this)
CMD ["mvn", "--version"]
