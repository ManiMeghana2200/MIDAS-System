package com.jpmc.midascore;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import com.jpmc.midascore.kafka.TransactionListener;

@SpringBootTest(properties = {
	    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
	    "general.kafka-topic=test-topic"
	})
@EmbeddedKafka(partitions = 1, topics = { "test-topic" })
@DirtiesContext
@TestPropertySource(properties = {
	    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
	    "general.kafka-topic=test-topic"
	})
class TaskTwoTests {
    static final Logger logger = LoggerFactory.getLogger(TaskTwoTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private FileLoader fileLoader;

    @Test
    void task_two_verifier() throws InterruptedException {
    	//Thread.sleep(2000);
        String[] transactionLines = fileLoader.loadStrings("/test_data/poiuytrewq.uiop");
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }
        Thread.sleep(2000);
        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("----------------------------------------------------------");
        logger.info("use your debugger to watch for incoming transactions");
        logger.info("kill this test once you find the answer");
        
        System.out.println("FIRST FOUR AMOUNTS: " + TransactionListener.getReceivedAmounts());

        while (true) {
            Thread.sleep(2000);
            logger.info("...");
        }
    }

}
