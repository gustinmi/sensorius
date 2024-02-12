# Getting Started

## Facts from requirements

- system is real-time
- producers specify no key so hence there is round robin to partitions
- we want to prevent saving of same data reading as much as possible
- changing anything on Kafka is out of scope. We have no access to topic configuration, message format, etc
- application is real-time so we save and keep data in best effort manner. We discard old stale data (because some sensor was offline for considerable amount of time or had latency problems)
- since we are only interested in change in temperature, we avoid save same readings (in respect to given time frame)

## Implementation decisions based on input document

- core engine was detached from Kafka in order for us to be able to use simulation engines in tests
- there was no time to benchmark Spring CDI container, so core functionality is more or less written in POJOs. It should be faster.
- since performance is most important, i decided to go with console profile (no embedded webserver). This will of course mean some of the spring monitoring functionalities will not be available

## Code quality tools

- TDD from beginning
- Eclipse coverage tools was used to get insight into how much code is covered with test

## Testing

I prepared simple load simulation inside unit tests. We can adjust frequency of simulation. Another way of testing is via JMeter producer or scripts.


## Code instrumentation

- all important parts of code are configurable
- final keyword is used where ever possible to minimise logical errors, reassignments, etc.
- conditional compilations is used for logging. Usually on high loads, we do not verbose or info log.
- there is possibility to use debugging an all pieces of application


## References

### Testing

[Load testing kafka producer and consumer](https://www.blazemeter.com/blog/kafka-testing)
