# multi-stage build for the Spring Boot backend
# Use JDK 21 images to match the project's Java version
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# copy only pom first to leverage cache
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
