package ming.test.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;


@SpringBootTest
class KafkaApplicationTests {


	@Autowired
	private KafkaTemplate kafkaTemplate;

	@Test
	void contextLoads() throws InterruptedException {
		Random random = new Random();

		for (int i = 0; i < 60; i++) {
//			kafkaTemplate.send("parti2", 2, null, "form spring boot "+i);
			kafkaTemplate.send("parti2", "form spring boot "+i);
			System.out.println("send message" + i);
			Thread.sleep(1000);
		}


	}

}
