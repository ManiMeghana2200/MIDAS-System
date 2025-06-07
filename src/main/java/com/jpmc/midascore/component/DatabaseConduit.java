package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.Incentive;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DatabaseConduit {
    private final UserRepository userRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConduit.class);


    public DatabaseConduit(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public void save(UserRecord user) {
        userRepository.save(user);
    }

    public void save(Transaction transaction) {
        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());
        

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            logger.warn("Invalid sender or recipient ID in transaction: {}", transaction);
            return;
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        float amount = transaction.getAmount();
        if (sender.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            logger.warn("Sender has insufficient balance for transaction: {}", transaction);
            return;
        }

        // Deduct from sender
        sender.setBalance(sender.getBalance().subtract(BigDecimal.valueOf(amount)));

        // Call Incentive API
        float incentiveAmount = 0;
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Incentive> response = restTemplate.postForEntity(
                "http://localhost:8080/incentive",
                transaction,
                Incentive.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                incentiveAmount = response.getBody().getAmount();
            }
        } catch (Exception e) {
            logger.error("Error contacting Incentive API: {}", e.getMessage());
        }

        // Add to recipient's balance
        recipient.setBalance(
        	    recipient.getBalance()
        	        .add(BigDecimal.valueOf(amount))
        	        .add(BigDecimal.valueOf(incentiveAmount))
        	);


        // Save both user records
        userRepository.save(sender);
        userRepository.save(recipient);
    }
    
    public void printWilburBalance() {
        Optional<UserRecord> wilburOpt = userRepository.findByName("wilbur"); // assumes you have this method

        if (wilburOpt.isPresent()) {
            BigDecimal balance = wilburOpt.get().getBalance();
            int rounded = balance.setScale(0, RoundingMode.FLOOR).intValue();
            System.out.println("Wilbur's balance (rounded down): " + rounded);
        } else {
            System.out.println("Wilbur not found.");
        }
    }



}
