FROM molgenis/maven-jdk17

WORKDIR /app

COPY ./src ./src
COPY ./mvnw ./mvnw
COPY ./mvnw.cmd ./mvnw.cmd
COPY ./pom.xml ./pom.xml

RUN mvn package

ENTRYPOINT ["java", "-jar", "target/GeeksSocialNetworkAPI-0.0.1-SNAPSHOT.jar"]