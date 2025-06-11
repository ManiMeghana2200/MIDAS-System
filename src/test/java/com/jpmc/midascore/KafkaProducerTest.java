package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test-topic"}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092", "port=9092"
})
public class KafkaProducerTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    private final CountDownLatch latch = new CountDownLatch(1);

    private volatile Transaction receivedTransaction;

    @KafkaListener(topics = "test-topic", groupId = "midas-group")
    public void listen(Transaction tx) {
        System.out.println("Received transaction: " + tx);
        receivedTransaction = tx;
        latch.countDown();
    }

    @Test
    public void testSend() throws InterruptedException {
        kafkaProducer.send("1, 2, 100.0");

        boolean messageReceived = latch.await(5, TimeUnit.SECONDS); // Wait for message
        assertTrue(messageReceived, "Kafka message was not received in time");

        // Optionally assert contents
        assertTrue(receivedTransaction != null);
        assertTrue(receivedTransaction.getAmount() == 1L);
        assertTrue(receivedTransaction.getAmount() == 2L);
        assertTrue(receivedTransaction.getAmount() == 100.0f);
    }
}
