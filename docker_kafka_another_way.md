
# Another way to setup kafka brokers
### setup kafka brokers

```
### setup kafka_1
docker run --name kafka_1 \
--link zookeeper \
-p 19092:19092 \
-e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka_1:9092,CONNECTIONS_FROM_HOST://kafka_1:19092 \
-e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONNECTIONS_FROM_HOST:PLAINTEXT \
-e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
--network kafkanet \
confluentinc/cp-kafka

### setup kafka_2
docker run --name kafka_2 \
--link zookeeper \
-p 29092:29092 \
-e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka_2:9092,CONNECTIONS_FROM_HOST://kafka_2:29092 \
-e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONNECTIONS_FROM_HOST:PLAINTEXT \
-e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
--network kafkanet \
confluentinc/cp-kafka

### setup kafka_3
docker run --name kafka_3 \
--link zookeeper \
-p 39092:39092 \
-e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka_3:9092,CONNECTIONS_FROM_HOST://kafka_3:39092 \
-e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONNECTIONS_FROM_HOST:PLAINTEXT \
-e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
--network kafkanet \
confluentinc/cp-kafka
```
