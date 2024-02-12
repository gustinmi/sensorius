# Docker support

## Gradle tasks

```
./gradlew clean
./gradlew bootJar
```

## Running and building docker

```
 docker build -f Dockerfile-build -t sensorius .
docker build -t sensorius .
docker run sensorius

```