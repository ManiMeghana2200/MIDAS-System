package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Balance;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BalanceQuerier {
	
	private String balanceServiceUrl;
	@LocalServerPort
    private int port;
	
	@Value("${balance.service.url}")
    private String baseUrl;
	
    private final RestTemplate restTemplate;

    public BalanceQuerier(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }
    
    @PostConstruct
    public void init() {
        this.balanceServiceUrl = "http://localhost:" + port + "/balance";
    }

    public Balance query() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(baseUrl + "/balance", Balance.class);
    }
}
