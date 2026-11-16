FROM eclipse-temurin:17-jre
WORKDIR /app
COPY smart-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]