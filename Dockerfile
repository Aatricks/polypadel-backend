# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ===== Runtime stage =====
# Image compatible Mac M1/M2/M3 (Ubuntu based)
FROM eclipse-temurin:17-jre
WORKDIR /app

# On copie le JAR construit
COPY --from=build /app/target/polypadel-backend-0.0.1-SNAPSHOT.jar app.jar

# On expose le port
EXPOSE 8080

# On lance l'application (en root, c'est OK pour le dev)
ENTRYPOINT ["java", "-jar", "app.jar"]