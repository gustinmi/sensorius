# Getting Started

## Facts from requirements

- Application is real-time so we save and keep data in best effort manner. We discard old stale data (because some sensor was offline for considerable amount of time or had latency problems)
- Producers specify no key so hence there is round robin producing to partitions (as per spec)
- Changing anything on Kafka configuration and producer side is out of scope (topic configuration, message format, keys, headers)
- Since we are only interested in change in temperature, we avoid save same readings 

## Implementation decisions based on input document

- Realtime data is processed in time-based frames (if same reading is repeated in whole time frame, only 1 reading is actually saved)
- If however sensors readings is increased (bigger resolution), we save when reading count reaches treshold
- Due to high partition count we'll use concurrent listeners.
- Core engine was detached from Kafka in order for us to be able to use simulation engines in junit tests.
- There was no time to benchmark Spring, so core functionality is more or less written in POJOs. It should be faster.
  for similar reasons, no spring framework libraries were used (like quartz for cronjob like tasks)
- Since performance is most important, i decided to go with console profile (no embedded webserver). This will of course mean some of the spring monitoring functionalities will not be available.
- Incremental testing (no Kafka, embedded Kafka broker, real Kafka broker); all important components can be tested in standalone mode.

###  Points for improvements 

- More concurrency
	- currently processing of messages and saving to timeseries db is done on same thread (although in thread save manner). Perhaps there should be per sensor backgroud flush detection
	- perhaps timeseries db would have its own background thread to process buffer of persistence candidates
- Add spring metrics plugin (actuator) for beter integration into kubernetes (healtcheck, probe and so on ...)
- Add gracefull shutdown listener for all components. Kafka listener should send everyting to timeseriesDB, and timeseries should flush all it has

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
- simple http interface was added for health check probe
- simple time measurements were added on data insert for beter tracking. If used in conjuction with grafana or splunk, we could have error alerting if processing time rises

## Tools used

- Eclipse with STS Spring Tools v4
- JMeter
- Docker
- Kafka 

### Testing

[Load testing kafka producer and consumer](https://www.blazemeter.com/blog/kafka-testing)
