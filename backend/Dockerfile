# syntax=docker/dockerfile:1

# ---- Build stage: full JDK + Maven wrapper, produces the jar ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/

# Cache mount keeps ~/.m2 between builds, so dependencies aren't re-downloaded every time.
# Tests are skipped here: they need a database; they run separately (Step 6).
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q package -DskipTests && cp target/*.jar app.jar

# ---- Runtime stage: JRE only (no compiler, no Maven, no source code) ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# Don't run as root inside the container
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/app.jar app.jar
USER app

EXPOSE 8080
# Size the heap from the container's memory limit instead of the host machine's RAM
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
