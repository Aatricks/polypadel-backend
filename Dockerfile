# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the POM first to cache dependencies
COPY pom.xml .

# DOWNLOAD dependencies with a local cache mount. 
# This drastically speeds up re-builds by persisting the local Maven repo between Docker builds.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -e -DskipTests dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# BUILD the application. 
# We use the same cache mount so we don't re-download plugins/dependencies.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests package

# ===== Runtime stage =====
# Use the Alpine variant (much smaller size)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security (best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar. 
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# We set a default ENV to avoid null errors
ENV JAVA_OPTS=""

# Alpine has 'sh', so this works.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]