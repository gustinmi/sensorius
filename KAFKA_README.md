# SENSORIUS


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

# IMPORTNAT: for docker and WSL
export _JAVA_OPTIONS="-Djava.net.preferIPv4Stack=true"

# run kafka server
bin/kafka-server-start.sh config/kraft/server.properties

# create topic
bin/kafka-topics.sh --create --topic demo-messages --bootstrap-server localhost:9092

# produce some messages
bin/kafka-console-producer.sh --topic demo-messages --bootstrap-server localhost:9092

# consume events
bin/kafka-console-consumer.sh --topic demo-messages --from-beginning --bootstrap-server localhost:9092

# stop testing

# 1. Stop the consumer and producer clients with Ctrl+C
# 2. Stop the Kafka Server with Ctrl+C

rm -rf /tmp/kafka-logs /tmp/zookeeper /tmp/kraft-combined-logs

```