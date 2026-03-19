package com.fraud.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RiskScore {
    private BigDecimal fraud_probability;
    private String risk_level;
}
