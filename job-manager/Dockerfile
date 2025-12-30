# --- 1. ビルドをする段階 (Builder) ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# プロジェクトの材料をコピー
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]