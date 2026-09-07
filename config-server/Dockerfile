FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY book-service/pom.xml book-service/
COPY loan-service/pom.xml loan-service/

RUN mvn -q -pl config-server -am dependency:go-offline

COPY config-server config-server
RUN mvn -q -pl config-server -am clean package -DskipTests

# ---- Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /build/config-server/target/config-server-1.0.0-SNAPSHOT.jar app.jar
# config-repo est servi en mode natif depuis /config-repo (voir SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS)
COPY config-repo /config-repo

RUN chown -R app:app /app /config-repo
USER app

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
