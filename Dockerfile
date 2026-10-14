# One Dockerfile for every service. Build with: --build-arg MODULE=catalog-service
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
ARG MODULE
RUN mvn -B -q -pl ${MODULE} -am -DskipTests package

FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
COPY --from=build /app/${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
