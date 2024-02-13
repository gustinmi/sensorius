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

## Docker compose

### check config

```bash
docker-compose config
```

### build image
```bash
docker-compose up --build
```