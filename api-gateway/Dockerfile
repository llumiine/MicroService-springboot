FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY book-service/pom.xml book-service/
COPY loan-service/pom.xml loan-service/

RUN mvn -q -pl api-gateway -am dependency:go-offline

COPY api-gateway api-gateway
RUN mvn -q -pl api-gateway -am clean package -DskipTests

# ---- Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /build/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
