package com.jpmc.midascore.kafka;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TransactionListener {
	@Autowired
	private TransactionService transactionService;

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    // Thread-safe list to store first 4 amounts
    private static final List<Float> receivedAmounts = Collections.synchronizedList(new ArrayList<>());

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);

        // Only store the first 4 amounts
        if (receivedAmounts.size() < 4) {
            receivedAmounts.add(transaction.getAmount());
        }
        
        transactionService.processTransaction(transaction);

    }

    // Used by the test framework to verify the result
    public static List<Float> getReceivedAmounts() {
        return new ArrayList<>(receivedAmounts);
    }

    // Optional: Clear for other tests (not needed for this one)
    public static void reset() {
        receivedAmounts.clear();
    }
    
    
}
