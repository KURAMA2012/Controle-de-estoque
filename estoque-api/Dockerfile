# ---- build ----
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---- runtime ----
FROM eclipse-temurin:8-jre
WORKDIR /app
RUN groupadd -r app && useradd -r -g app app
COPY --from=build /build/target/*.jar /app/estoque-api.jar
USER app
EXPOSE 7009
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/estoque-api.jar"]