package com.jpmc.midascore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
public class BalanceController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public Balance getBalance(@RequestParam("userId") long userId) {
        Optional<UserRecord> userOpt = userRepository.findById(userId);
        float amount = userOpt.map(u -> u.getBalance().floatValue()).orElse(0f);
        return new Balance(amount);
    }
}
