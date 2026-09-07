FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY loan-service/pom.xml loan-service/

RUN mvn -q -pl loan-service -am dependency:go-offline

COPY . .
RUN mvn -q -pl loan-service -am clean package -DskipTests

# ---- Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /build/loan-service/target/loan-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]