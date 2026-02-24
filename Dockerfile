# multi-stage build for the Spring Boot backend
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# copy only pom first to leverage cache
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# runtime image
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
