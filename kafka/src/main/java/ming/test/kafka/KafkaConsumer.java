package ming.test.kafka;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.event.NonResponsiveConsumerEvent;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Component
public class KafkaConsumer {
    private static Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @Value("${ming.test.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 20*1000);
        return props;
    }


    @KafkaListener(id = "g2", topics = "parti3")
    public void listen(String message) {
        System.out.println("g2 consumer get message = "+ message);
//        try {
//            Thread.sleep(60000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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
}
