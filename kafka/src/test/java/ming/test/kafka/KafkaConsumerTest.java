package ming.test.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

@Import(KafkaConsumer.class)
@SpringBootTest
class KafkaConsumerTest {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Test
    void listen() throws InterruptedException {
        MessageListenerContainer g2 = registry.getListenerContainer("g2");// get instance of ming.test.kafka.KafkaConsumer.listen
        while (g2.isRunning()) {
            Thread.sleep(1000);
        }
        Thread.sleep(1000);
        System.out.println("kafka listener stopped");
    }
}