# Getting Started

## Implementation decisions based on input document

- application is realtime so we save and keep data in best effor maner. There is no sense in saving old stale data because some sensor was offline for considerable amount of time

- since we are only interested in change in temperature, we do not save same readings for given time frame

```java
return this.temperature.compareTo(that.temperature);
```

- core engine was detached from kafka in order for us to be able to use simulation engines



## Code quality tools

- Eclipse coverage tools was used to get insight into how much code is covered with test


## Testing

A simple load simulation. We can adjust frequency of simulation.


## Code instrumentation

- conditional compilations is used for logging. Usually on high loads, we do not verbose or info log


## References

### Testing

[Load testing kafka producer and consumer](https://www.blazemeter.com/blog/kafka-testing)

### Kubernetes

[](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
