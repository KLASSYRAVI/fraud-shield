package com.fraud.service;

import com.fraud.dto.RiskScore;
import com.fraud.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudScoringService {

    private final RestTemplate restTemplate;

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    public RiskScore scoreTransaction(Transaction transaction) {
        try {
            String url = mlServiceUrl + "/predict";
            Map<String, Object> request = new HashMap<>();
            request.put("amount", transaction.getAmount());
            request.put("hour_of_day", transaction.getHourOfDay());
            request.put("is_foreign_location", transaction.getIsForeignLocation() != null && transaction.getIsForeignLocation() ? 1 : 0);
            
            int deviceType = 1; // 0=mobile, 1=desktop, 2=tablet
            if ("mobile".equalsIgnoreCase(transaction.getDevice())) deviceType = 0;
            else if ("tablet".equalsIgnoreCase(transaction.getDevice())) deviceType = 2;
            
            request.put("device_type", deviceType);
            request.put("transactions_last_hour", transaction.getTransactionsLastHour());

            RiskScore score = restTemplate.postForObject(url, request, RiskScore.class);
            return score;
        } catch (Exception e) {
            log.error("Failed to call ML service: ", e);
            // Default response if ML service is down
            RiskScore defaultScore = new RiskScore();
            defaultScore.setFraud_probability(new java.math.BigDecimal("0.0"));
            defaultScore.setRisk_level("LOW");
            return defaultScore;
        }
    }
}
