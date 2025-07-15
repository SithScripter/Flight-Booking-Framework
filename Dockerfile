# ✅ Base image with Maven and JDK 21 (Temurin distribution)
FROM maven:3.9-eclipse-temurin-21

# ✅ Optional: set working directory
WORKDIR /usr/src/app

# ✅ Optional command for debug if needed (Jenkins overrides this anyway)
CMD ["mvn", "--version"]