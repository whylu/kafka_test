### add to docker host's /etc/hosts 
```
127.0.0.1 kafka_1
127.0.0.1 kafka_2
127.0.0.1 kafka_3
```

### add network for test
```
docker network create --driver bridge kafkanet
```

### setup zookeeper
```
docker run --name zookeeper -p 2181:2181 --network kafkanet zookeeper
```

### setup kafka brokers

```
### setup kafka_1
docker run --name kafka_1 \
--link zookeeper \
-p 19092:19092 \
-e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
-e KAFKA_LISTENERS=PLAINTEXT://kafka_1:19092 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka_1:19092 \
-e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
--network kafkanet \
confluentinc/cp-kafka


### setup kafka_2
docker run --name kafka_2 \
--link zookeeper \
-p 29092:29092 \
-e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
-e KAFKA_LISTENERS=PLAINTEXT://kafka_2:29092 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka_2:29092 \
-e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
--network kafkanet \
confluentinc/cp-kafka

### setup kafka_3
docker run --name kafka_3 \
--link zookeeper \
-p 39092:39092 \
-e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
-e KAFKA_LISTENERS=PLAINTEXT://kafka_3:39092 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka_3:39092 \
-e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
--network kafkanet \
confluentinc/cp-kafka

```

### show brokers
zookeeper-shell localhost:2181 ls /brokers/ids


### create topic 
```
kafka-topics --create \
 --replication-factor 2 \
 --partitions 2 \
 --zookeeper localhost:2181 \
 --topic parti2
```

### consumer
```
kafka-console-consumer --topic parti2 --bootstrap-server localhost:19092,localhost:29092 --group g1
```

### write message
```
kafka-console-producer --topic parti2 --bootstrap-server localhost:19092,localhost:29092
```

### show topics
```
kafka-topics --list --zookeeper localhost:2181
```

### show topic info
```
kafka-topics --describe --zookeeper localhost:2181 --topic parti2
```

### change topic partitions
```
kafka-topics --zookeeper localhost:2181 --alter --topic parti2 --partitions 3
```



# ref
- https://www.confluent.io/blog/kafka-listeners-explained/
- https://www.confluent.io/blog/kafka-client-cannot-connect-to-broker-on-aws-on-docker-etc/
