FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
COPY wsdl ./wsdl

RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /workspace/target/wifi-admin-0.0.1-SNAPSHOT.jar /app/wifi-admin.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/wifi-admin.jar"]
