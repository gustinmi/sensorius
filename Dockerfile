# First stage: build the application
FROM  gradle:jdk17-focal as build 
WORKDIR /workspace/app

COPY build.gradle build.gradle
COPY build.gradle build.gradle
COPY src src

RUN gradle build -x test 

# Second stage: create a slim image
FROM openjdk:17
ARG DEPENDENCY=/workspace/app/build/libs
COPY --from=build ${DEPENDENCY}/app-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

# TROUBLESHOOTING
#ENTRYPOINT ["java", "-version"]
#ENTRYPOINT ["ls", "-la"]
#CMD ["sleep", "infinity"]