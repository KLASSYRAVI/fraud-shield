package com.fraud.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardTrends {
    private List<HourlyData> hourlyData;
    private List<String> topRiskyLocations;
    private Map<String, Integer> deviceBreakdown;

    @Data
    @Builder
    public static class HourlyData {
        private String hour;
        private int total;
        private int fraudCount;
        private double fraudRate;
    }
}
