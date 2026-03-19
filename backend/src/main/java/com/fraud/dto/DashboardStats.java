package com.fraud.dto;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DashboardStats {
    private long totalTransactions;
    private long flaggedCount;
    private double fraudRate;
    private double avgRiskScore;
}
