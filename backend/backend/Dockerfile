
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -B -DskipTests package

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar --server.port=${PORT:-8080}"]
