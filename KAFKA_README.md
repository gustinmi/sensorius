# SENSORIUS

-[Boot](https://spring.io/guides/gs/spring-boot)
-[Testing](https://docs.spring.io/spring-kafka/reference/testing.html)
-[Embedded kafka](https://docs.spring.io/spring-kafka/reference/testing.html#embedded-kafka-annotation)
-[Influx](https://medium.berkayozcan.com/connecting-an-influxdb-database-to-a-spring-boot-application-81f53ead3c7)
-[Influx sample](https://medium.com/trabe/monitoring-humidity-and-temperature-with-grafana-influxdb-and-orange-pi-9680046c70c)
-[Stress testing](https://www.blazemeter.com/blog/kafka-testing)
-[SpringBoot Docker](https://medium.com/@bubu.tripathy/dockerizing-your-spring-boot-application-75bf2c6568d0)
-[Kafka Docs](https://kafka.apache.org/0110/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html#assign(java.util.Collection))
-[SpringBoot logging](https://www.baeldung.com/spring-boot-logging)
-[SpringBoot Start project - actuator](https://spring.io/guides/gs/spring-boot)

## Installing kafka

** NOTE - running kafka docker has proven difficult in Windows WSL2 environment due to networking, thats why we're running here bare metal version**

[From](https://www.confluent.io/blog/set-up-and-run-kafka-on-windows-linux-wsl-2/#start-kafka-cluster)

```
# upgrade packages
sudo apt-get update && sudo apt-get upgrade -y

# install java
sudo apt install openjdk-17-jdk -y

# download kafka
wget https://dlcdn.apache.org/kafka/3.5.0/kafka_2.13-3.5.0.tgz

# unzip it
tar -xzf kafka_2.13-3.5.0.tgz
cd kafka_2.13-3.5.0

# generate cluster id
KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"

# format the log directories:
bin/kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c config/kraft/server.properties

# IMPORTANT: for docker and WSL (use ipv4)
export JAVA_OPTIONS="-Djava.net.preferIPv4Stack=true"
export KAFKA_OPTS="-Djava.net.preferIPv4Stack=True"

# run kafka server
bin/kafka-server-start.sh config/kraft/server.properties

# create topic
bin/kafka-topics.sh --create --topic sensor-data --bootstrap-server localhost:9092

# create topic with partitions
bin/kafka-topics.sh --create \
  --replication-factor 1 --partitions 1 \
  --topic mytopic

# produce some messages
bin/kafka-console-producer.sh --topic demo-messages --bootstrap-server localhost:9092

# consume events
bin/kafka-console-consumer.sh --topic demo-messages --from-beginning --bootstrap-server localhost:9092

# stop testing

# 1. Stop the consumer and producer clients with Ctrl+C
# 2. Stop the Kafka Server with Ctrl+C

rm -rf /tmp/kafka-logs /tmp/zookeeper /tmp/kraft-combined-logs

```


## Troubleshoot

### Broker not acessible

```
netstat -na | grep 9092
tcp6       0      0 :::9092                 :::*                    LISTEN

netstat -na | grep 9092
tcp        0      0 0
```



