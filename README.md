# Getting Started

## Facts from requirements

- Application is real-time so we process data in best effort manner. Main assumption is that if data  is not arriving in time succession it should be discarded. Only newer reading will be processed.
- Producers specify no key for messages so we have round robin producing to partitions (as per Kafka spec)
- Changing anything on Kafka configuration or on producer side is out of scope (topic configuration, message format, keys, headers)
- Since we are only interested in change in temperature, we avoid saving same readings 

## Implementation decisions based on input document

- Real-time data is processed in time-based frames (if same reading is repeated in whole time frame, only 1 reading is actually saved)
- If however sensors readings is increased (bigger resolution), we save when readings count reaches certain threshold
- Due to high partition count we'll use concurrent listeners.
- Core engine was detached from Kafka in order for us to be able to use simulation engines in junit tests.
- Because of the performance requirements, core functionality is more or less written in POJOs. In this was performance and memory consumption improvements will be be faster.
  for similar reasons, no additional spring framework libraries were used (like Quartz for cronjob like tasks or Actuator for metrics)
- Since performance is most important, i decided to go with SpringBoot console runner  (without spring web). This will of course mean some of the spring monitoring functionalities will not be available.
- Incremental testing (progressive scenario: 1) no Kafka, 2) embedded Kafka broker and 3) real Kafka broker). This menas all important components can be tested in standalone mode without any external dependencies.
- added basic support for gracefull shutdown

###  Points for improvements 

- More concurrency
	- currently processing of messages and saving to timeseries db is done on same thread (although in thread save manner). Perhaps timeseries db would have its own background thread to process buffers. This could also depend on the speed of timeseries db.
- Consider adding Spring metrics plugin (Actuator) for beter integration into kubernetes (healtcheck, probe and so on ...)
- Extend gracefull shutdown listener for all components. Kafka listener should send everyting to timeseriesDB, and timeseries should flush all it has in the case of shutdown
- Choose some lightning fast JSON processing library
- Further sessions with VisualVM or JMX console would improve lag when load is high.

## Code quality tools

- TDD from beginning
- Eclipse coverage tools was used to get insight into how much code is covered with test

## Testing

I prepared simple load simulation inside unit tests. We can adjust frequency of simulation. Another way of testing is via JMeter producer or scripts.

## Code instrumentation

- all important parts of code are configurable
- final keyword is used where ever possible to trade logical errors for syntax errors  (reassignments, missing branch statements)
- conditional compilations is used for logging. Usually on high loads, we do not verbose or info log.
- there is possibility to use debugging in all classes comprising application (without external dependency)
- simple HTTP interface was added for health check probe
- simple time measurements were added on data insert for beter tracking. If used in conjuction with grafana or splunk, we could have error alerting if processing time rises

## CICD support 

- a docker file **Dockerfile** is prepared. It build image in docker and starts it. This is basic input to continious delivery.

## Tools used

Developed on Windows with WSL2 running ubuntu 22.04

- Eclipse with STS Spring Tools v4
- JMeter (DI kafka plugin, dummy sampler plugin)
- Docker 
- Kafka 

### Testing

[Load testing kafka producer and consumer](https://www.blazemeter.com/blog/kafka-testing)

## Resources used

- [SpringBoot](https://spring.io/guides/gs/spring-boot)
- [Testing](https://docs.spring.io/spring-kafka/reference/testing.html)
- [Embedded kafka](https://docs.spring.io/spring-kafka/reference/testing.html#embedded-kafka-annotation)
- [SpringBoot Docker](https://medium.com/@bubu.tripathy/dockerizing-your-spring-boot-application-75bf2c6568d0)
- [SpringBoot logging](https://www.baeldung.com/spring-boot-logging)
- [SpringBoot Kafka] (https://www.baeldung.com/spring-kafka)
- [SpringBoot Start project - actuator](https://spring.io/guides/gs/spring-boot)
- [SpringBoot application.properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [Spring Kafka application.properties](https://gist.github.com/geunho/77f3f9a112ea327457353aa407328771)
- [Spring Kafka Error handling](https://medium.com/javarevisited/robust-kafka-consumer-error-handling-on-a-spring-boot-3-application-6fc95e92c956)
- [Kafka Docs](https://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html#assign(java.util.Collection))
- [Stress testing](https://www.blazemeter.com/blog/kafka-testing)

### Running app

#### Gradle tasks

```
./gradlew clean
./gradlew bootJar
./gradlew bootRun
```

#### Running and building inside docker

```
docker build -t sensorius .
docker build -t sensorius .
docker run sensorius
```

#### Building docker image simplistic

```
docker build -f Dockerfile-build -t sensorius .
docker build -t sensorius .
docker run sensorius
```

#### Docker compose

### check config

```bash
docker-compose config
```

### build image
```bash
docker-compose up --build
```