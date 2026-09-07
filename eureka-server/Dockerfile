FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY book-service/pom.xml book-service/
COPY loan-service/pom.xml loan-service/

RUN mvn -q -pl eureka-server -am dependency:go-offline

COPY eureka-server eureka-server
RUN mvn -q -pl eureka-server -am clean package -DskipTests

# ---- Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /build/eureka-server/target/eureka-server-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
