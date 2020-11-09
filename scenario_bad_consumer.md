# Scenario - consumer dead or slow

There are a topic with 2 partitions, consumed by a group with 2 consumer  

partition1 --> consumer A  
partition2 --> consumer B 

```
---------- partition1 ------------
 ... | m1.4 | m1.3 | m1.2 | m1.1  -------> consumer A
----------------------------------

---------- partition2 ------------
 ... | m2.4 | m2.3 | m2.2 | m2.1  -------> consumer B
----------------------------------
```

when consumer A is very slow or dead, those message in partition 1 is blocked



# how to handle this 
Set `max.poll.interval.ms` with a value which is unacceptable for the application.  
Consumer will raise a timeout event and send `LeaveGroup` request to broker.  
This means the time between subsequent calls to poll() was longer than the configured `max.poll.interval.ms`, which typically implies that the poll loop is spending too much time processing messages.   
You can address this either by increasing `max.poll.interval.ms` or by reducing the maximum size of batches returned in poll() with `max.poll.records`.


# Simulation

we have a topic `parti3` with 3 partition, and 1 group `g1` with 3 consumer, the consumer config 
```java
	props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 20*1000);
```

run two consumer with listen function:
```java

    @KafkaListener(id = "g2", topics = "parti3")
    public void listen(String message) {
        System.out.println("g2 consumer get message = "+ message);
    }
```

and one consumer with delay, to simulate break consumer
```java

    @KafkaListener(id = "g2", topics = "parti3")
    public void listen(String message) {
        System.out.println("g2 consumer get message = "+ message);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void nonResponsiveEventHandler(NonResponsiveConsumerEvent event) {
        log.warn("Consumer has become non-responsive, no poll for {} milliseconds. Listener {}, TopicPartitions {}, Consumer: {}",
                event.getTimeSinceLastPoll(), event.getListenerId(), event.getTopicPartitions(),
                event.getConsumer().toString());
        ConcurrentMessageListenerContainer container = event.getContainer(ConcurrentMessageListenerContainer.class);
        if(container!=null) {
            if(container.isRunning()) {
                log.info("now stopping listenerContainer {}", container.getListenerId());
                container.stop(()->{
                    log.info("listenerContainer {} stopped", container.getListenerId());
                });
            }

        }
    }

```
I will stop application when NonResponsiveConsumerEvent is triggered

## test
after all consumer started, we send message by kafka client:
```sh
kafka-console-producer --topic parti3 --bootstrap-server localhost:19092,localhost:29092
>1
>2
>3
>4
>5
>6
>7
>0
>1
>2
>3
>4
>5
>6
>7
>8
>9
>10
>11
>12
>13
>14
>15
```

## results:  

consumer 1 (no delay):
```sh
2020-11-09 11:37:36.322  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] (Re-)joining group
2020-11-09 11:37:36.344  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Successfully joined group with generation 28
2020-11-09 11:37:36.344  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Adding newly assigned partitions: parti3-2
2020-11-09 11:37:36.353  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Setting offset for partition parti3-2 to the committed offset FetchPosition{offset=35, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka_2:29092 (id: 1002 rack: null)], epoch=0}}
2020-11-09 11:37:36.354  INFO 35750 --- [       g2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : g2: partitions assigned: [parti3-2]
g2 consumer get message = 7
g2 consumer get message = 8
g2 consumer get message = 9
g2 consumer get message = 12
g2 consumer get message = 13
2020-11-09 11:38:15.376  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Attempt to heartbeat failed since group is rebalancing
2020-11-09 11:38:15.377  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Revoke previously assigned partitions parti3-2
2020-11-09 11:38:15.377  INFO 35750 --- [       g2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : g2: partitions revoked: [parti3-2]
2020-11-09 11:38:15.377  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] (Re-)joining group
2020-11-09 11:38:15.393  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Successfully joined group with generation 29
2020-11-09 11:38:15.393  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Adding newly assigned partitions: parti3-2
2020-11-09 11:38:15.400  INFO 35750 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Setting offset for partition parti3-2 to the committed offset FetchPosition{offset=40, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka_2:29092 (id: 1002 rack: null)], epoch=0}}
2020-11-09 11:38:15.401  INFO 35750 --- [       g2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : g2: partitions assigned: [parti3-2]

```

consumer 2 (no delay): 
```sh
2020-11-09 11:37:36.328  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] (Re-)joining group
2020-11-09 11:37:36.335  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Finished assignment for group at generation 28: {consumer-g2-1-7955b336-1002-4215-9d3d-086df8379af4=Assignment(partitions=[parti3-1]), consumer-g2-1-6ff23b45-0d38-46c5-b428-2e87f914ce24=Assignment(partitions=[parti3-0]), consumer-g2-1-9ee10b44-fa5b-4943-9434-e95f6ad764bf=Assignment(partitions=[parti3-2])}
2020-11-09 11:37:36.344  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Successfully joined group with generation 28
2020-11-09 11:37:36.344  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Adding newly assigned partitions: parti3-0
2020-11-09 11:37:36.354  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Setting offset for partition parti3-0 to the committed offset FetchPosition{offset=40, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka_3:39092 (id: 1003 rack: null)], epoch=0}}
2020-11-09 11:37:36.354  INFO 35748 --- [       g2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : g2: partitions assigned: [parti3-0]
g2 consumer get message = 4
g2 consumer get message = 5
g2 consumer get message = 6
2020-11-09 11:38:15.372  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Attempt to heartbeat failed since group is rebalancing
2020-11-09 11:38:15.372  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Revoke previously assigned partitions parti3-0
2020-11-09 11:38:15.372  INFO 35748 --- [       g2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : g2: partitions revoked: [parti3-0]
2020-11-09 11:38:15.372  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] (Re-)joining group
2020-11-09 11:38:15.383  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Finished assignment for group at generation 29: {consumer-g2-1-6ff23b45-0d38-46c5-b428-2e87f914ce24=Assignment(partitions=[parti3-0, parti3-1]), consumer-g2-1-9ee10b44-fa5b-4943-9434-e95f6ad764bf=Assignment(partitions=[parti3-2])}
2020-11-09 11:38:15.393  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Successfully joined group with generation 29
2020-11-09 11:38:15.393  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Adding newly assigned partitions: parti3-0, parti3-1
2020-11-09 11:38:15.400  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Setting offset for partition parti3-0 to the committed offset FetchPosition{offset=43, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka_3:39092 (id: 1003 rack: null)], epoch=0}}
2020-11-09 11:38:15.400  INFO 35748 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Setting offset for partition parti3-1 to the committed offset FetchPosition{offset=33, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka_1:19092 (id: 1001 rack: null)], epoch=0}}
2020-11-09 11:38:15.400  INFO 35748 --- [       g2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : g2: partitions assigned: [parti3-0, parti3-1]
g2 consumer get message = 0
g2 consumer get message = 1
g2 consumer get message = 2
g2 consumer get message = 3
g2 consumer get message = 10
g2 consumer get message = 11
g2 consumer get message = 14
g2 consumer get message = 15
```

consumer 3 (with delay): 
```sh
2020-11-09 11:37:34.679  INFO 35845 --- [       g2-0-C-1] org.apache.kafka.clients.Metadata        : [Consumer clientId=consumer-g2-1, groupId=g2] Cluster ID: vTeijbTcRMaqoYXC96JmPw
2020-11-09 11:37:34.681  INFO 35845 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Discovered group coordinator kafka_1:19092 (id: 2147482646 rack: null)
2020-11-09 11:37:34.684  INFO 35845 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] (Re-)joining group
2020-11-09 11:37:34.701  INFO 35845 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Join group failed with org.apache.kafka.common.errors.MemberIdRequiredException: The group member needs to have a valid member id before actually entering a consumer group
2020-11-09 11:37:34.701  INFO 35845 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] (Re-)joining group
2020-11-09 11:37:36.344  INFO 35845 --- [       g2-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Successfully joined group with generation 28
2020-11-09 11:37:36.347  INFO 35845 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Adding newly assigned partitions: parti3-1
2020-11-09 11:37:36.362  INFO 35845 --- [       g2-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Setting offset for partition parti3-1 to the committed offset FetchPosition{offset=33, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[kafka_1:19092 (id: 1001 rack: null)], epoch=0}}
2020-11-09 11:37:36.363  INFO 35845 --- [       g2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : g2: partitions assigned: [parti3-1]
g2 consumer get message = 0
2020-11-09 11:38:14.037  INFO 35845 --- [eat-thread | g2] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-g2-1, groupId=g2] Member consumer-g2-1-7955b336-1002-4215-9d3d-086df8379af4 sending LeaveGroup request to coordinator kafka_1:19092 (id: 2147482646 rack: null) due to consumer poll timeout has expired. This means the time between subsequent calls to poll() was longer than the configured max.poll.interval.ms, which typically implies that the poll loop is spending too much time processing messages. You can address this either by increasing max.poll.interval.ms or by reducing the maximum size of batches returned in poll() with max.poll.records.
2020-11-09 11:38:34.460  WARN 35845 --- [TaskScheduler-1] ming.test.kafka.KafkaConsumer            : Consumer has become non-responsive, no poll for 44997 milliseconds. Listener g2-0, TopicPartitions [parti3-1], Consumer: org.apache.kafka.clients.consumer.KafkaConsumer@713f7c4a
2020-11-09 11:38:34.460  INFO 35845 --- [TaskScheduler-1] ming.test.kafka.KafkaConsumer            : now stopping listenerContainer g2
kafka listener stopped
```
