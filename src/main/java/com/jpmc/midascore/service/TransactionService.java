package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {
	

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public boolean processTransaction(Transaction txn) {
        
    	UserRecord sender = userRepository.findById(txn.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(txn.getRecipientId()).orElse(null);

        if (sender == null || recipient == null) return false;

        BigDecimal amount = BigDecimal.valueOf(txn.getAmount());
        if (sender.getBalance().compareTo(amount) < 0) return false;

        // Update balances
        sender.setBalance(sender.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));

        userRepository.save(sender);
        userRepository.save(recipient);

        // Record transaction
        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(amount);
        record.setTimestamp(LocalDateTime.now());

        transactionRepository.save(record);

        return true;
    }

    public BigDecimal getTotalTransactionAmount() {
        return transactionRepository.findAll()
                .stream()
                .map(TransactionRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
