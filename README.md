# Getting Started

## Facts from requirements

- Application is real-time so we process data in best effort manner. Main assumption is, that data that is not arriving in time succession should be discarded
	- This means we discard old stale data, usually due to some sensor was offline for considerable amount of time or had latency problems
- Producers specify no key so hence there is round robin producing to partitions (as per spec)
- Changing anything on Kafka configuration or producer side is out of scope (topic configuration, message format, keys, headers)
- Since we are only interested in change in temperature, we avoid save same readings 

## Implementation decisions based on input document

- Real-time data is processed in time-based frames (if same reading is repeated in whole time frame, only 1 reading is actually saved)
- If however sensors readings is increased (bigger resolution), we save when readings count reaches certain treshold
- Due to high partition count we'll use concurrent listeners.
- Core engine is detached from Kafka in order for us to be able to use simulation engines in junit tests.
- There was no time to benchmark Spring, so core functionality is more or less written in POJOs. It should be faster.
  - for similar reasons, no additional spring framework libraries were used (like Quartz for cronjob like tasks or Actuator for metrics)
- Since performance is most important, I decided to go with Spring console runner (and custom minimalistic embedded webserver). This will of course mean some of the Spring monitoring functionalities will not be available.
- Incremental testing (progressive scenario: 1) no Kafka, 2) embedded Kafka broker and 3) real Kafka broker). This menas all important components can be tested in standalone mode without any external dependencies.
- added basic support for gracefull shutdown

###  Points for improvements 

- More concurrency
	- currently processing of messages and saving to timeseries db is done on same thread (although in thread save manner). Perhaps there should be per sensor backgroud flush detection
	- perhaps timeseries db would have its own background thread to process buffer of persistence candidates. This depends on the speed of timeseries db.
- Add spring metrics plugin (Actuator) for beter integration into kubernetes (healtcheck, probe and so on ...)
- Extend gracefull shutdown listener for all components. Kafka listener should send everyting to timeseriesDB, and timeseries should flush all it has in the case of shutdown
- Choose some lightning fast json processing library

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

## Tools used

- Eclipse with STS Spring Tools v4
- JMeter
- Docker
- Kafka 

### Testing

[Load testing kafka producer and consumer](https://www.blazemeter.com/blog/kafka-testing)
