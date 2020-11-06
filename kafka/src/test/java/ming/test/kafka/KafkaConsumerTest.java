package ming.test.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(KafkaConsumer.class)
@SpringBootTest
class KafkaConsumerTest {

    @Test
    void listen() throws InterruptedException {
        while (true) {
            Thread.sleep(1000);
        }
    }
}