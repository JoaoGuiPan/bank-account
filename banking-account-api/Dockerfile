FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY gradle/ gradlew ./gradle/
COPY gradlew.bat .
COPY . .
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew gradlew.bat
RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
