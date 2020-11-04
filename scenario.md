```
# Scenario 1
 2 partitions, 1 consumer 
 	-> consumer recieves all messages from partition1 and partition2
 partition1 	group1		consumer1
 partition2

# Scenario 2
 1 partitions 2 group, 1 consumer each group, 
	-> all consumer recieve the same messages
 partition1 	group1		consumer1
 	     	    group2		consumer2

# Scenario 3
 2 partitions 2 consumers
 	-> consumer1-1 get message from partition1
 	   consumer1-2 get message from partition2
 	   consumer1-3 get nothing

 partition1    group1      consumer1-1
 partition2                consumer1-2
 	                       consumer1-3

# Scenario 4
 3 partitions 2 consumers
 -> 2 consumers will consume all message from 3 patitions

 partition1    group1      consumer1-1
 partition2                consumer1-2
 partition3


```