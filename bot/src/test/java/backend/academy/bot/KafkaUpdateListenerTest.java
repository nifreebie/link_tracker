package backend.academy.bot;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import backend.academy.bot.model.EventType;
import backend.academy.bot.model.dto.request.LinkUpdateRequest;
import backend.academy.bot.service.UpdateService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

@SpringBootTest(
        properties = {
            "spring.kafka.consumer.auto-offset-reset=earliest",
            "app.topics.updates=test-updates-topic",
            "app.topics.dlq=dlq"
        })
@Testcontainers
public class KafkaUpdateListenerTest {

    @Container
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry reg) {
        reg.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        public UpdateService updateService() {
            return Mockito.mock(UpdateService.class);
        }

        @Bean
        public NewTopic updatesTopic(@Value("${app.topics.updates}") String t) {
            return TopicBuilder.name(t).partitions(1).replicas(1).build();
        }

        @Bean
        public NewTopic dlqTopic(@Value("${app.topics.dlq}") String t) {
            return TopicBuilder.name(t).partitions(1).replicas(1).build();
        }
    }

    @Autowired
    private UpdateService updateService;

    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        DefaultKafkaProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(props);
        this.kafkaTemplate = new KafkaTemplate<>(pf);
    }

    @Test
    void validMessage_isConsumedAndProcessed() {
        LinkUpdateRequest request = new LinkUpdateRequest(
                123L,
                "https://example.com",
                "описание",
                List.of(111L, 222L),
                "Заголовок",
                "bot_user",
                LocalDateTime.now(),
                EventType.COMMIT);

        ArgumentCaptor<LinkUpdateRequest> captor = ArgumentCaptor.forClass(LinkUpdateRequest.class);

        kafkaTemplate.send("test-updates-topic", request);
        kafkaTemplate.flush();

        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> verify(updateService).sendUpdates(captor.capture()));

        LinkUpdateRequest actual = captor.getValue();
        assertEquals(request.id(), actual.id());
        assertEquals(request.url(), actual.url());
        assertEquals(request.description(), actual.description());
        assertEquals(request.tgChatIds(), actual.tgChatIds());
        assertEquals(request.title(), actual.title());
        assertEquals(request.username(), actual.username());
        assertEquals(request.date(), actual.date());
        assertEquals(request.eventType(), actual.eventType());
    }

    @Test
    void whenInvalidJson_thenSentToDlq() {
        String badJson = "this is not valid JSON";

        kafkaTemplate.send("test-updates-topic", badJson);
        kafkaTemplate.flush();

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-test-group-1");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (Consumer<Object, Object> consumer = new DefaultKafkaConsumerFactory<>(consumerProps).createConsumer()) {
            consumer.subscribe(Collections.singleton("dlq"));
            consumer.subscribe(Collections.singleton("dlq"));
            ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofSeconds(10));
            assertThat(records.iterator().next().value()).isEqualTo("\"this is not valid JSON\"");
            verifyNoInteractions(updateService);
        }
    }
}
