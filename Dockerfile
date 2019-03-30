FROM openjdk:8-jdk-alpine
COPY ./target/ingestion-1.0.1-RELEASE.jar /usr/src/java/
WORKDIR /usr/src/java
EXPOSE 8080

CMD ["java", "-jar", "ingestion-1.0.1-RELEASE.jar"]