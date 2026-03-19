package com.fraud.controller;

import com.fraud.dto.DashboardStats;
import com.fraud.dto.DashboardTrends;
import com.fraud.model.Transaction;
import com.fraud.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics and reports")
public class DashboardController {

    private final TransactionService transactionService;

    @Operation(summary = "Get basic dashboard stats")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        long total = transactionService.getTotalTransactions();
        long flagged = transactionService.getFlaggedCount();
        
        double fraudRate = total == 0 ? 0 : (double) flagged / total * 100.0;
        double avgRisk = transactionService.getAverageRiskScore();

        return ResponseEntity.ok(DashboardStats.builder()
                .totalTransactions(total)
                .flaggedCount(flagged)
                .fraudRate(Math.round(fraudRate * 100.0) / 100.0)
                .avgRiskScore(Math.round(avgRisk * 10000.0) / 10000.0)
                .build());
    }

    @Operation(summary = "Get analytics trends for charts")
    @GetMapping("/trends")
    public ResponseEntity<DashboardTrends> getTrends() {
        List<Transaction> recentTx = transactionService.getTransactionsLast24Hours();

        // 1. Hourly Data
        Map<String, DashboardTrends.HourlyData> hourlyMap = new TreeMap<>();
        for (Transaction tx : recentTx) {
            String hour = String.format("%02d:00", tx.getHourOfDay());
            hourlyMap.putIfAbsent(hour, DashboardTrends.HourlyData.builder().hour(hour).build());
            DashboardTrends.HourlyData hd = hourlyMap.get(hour);
            hd.setTotal(hd.getTotal() + 1);
            if (Boolean.TRUE.equals(tx.getFlagged())) {
                hd.setFraudCount(hd.getFraudCount() + 1);
            }
        }
        for (DashboardTrends.HourlyData hd : hourlyMap.values()) {
            hd.setFraudRate(hd.getTotal() == 0 ? 0 : Math.round(((double) hd.getFraudCount() / hd.getTotal() * 100) * 100.0) / 100.0);
        }

        // 2. Top Risky Locations
        Map<String, Long> locationFraudCounts = recentTx.stream()
                .filter(t -> Boolean.TRUE.equals(t.getFlagged()))
                .collect(Collectors.groupingBy(Transaction::getLocation, Collectors.counting()));
        
        List<String> topLocations = locationFraudCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 3. Device Breakdown
        Map<String, Integer> deviceCount = new HashMap<>();
        for (Transaction tx : recentTx) {
            String dev = tx.getDevice() != null ? tx.getDevice().toLowerCase() : "unknown";
            deviceCount.put(dev, deviceCount.getOrDefault(dev, 0) + 1);
        }

        DashboardTrends trends = DashboardTrends.builder()
                .hourlyData(new ArrayList<>(hourlyMap.values()))
                .topRiskyLocations(topLocations)
                .deviceBreakdown(deviceCount)
                .build();
        
        return ResponseEntity.ok(trends);
    }

    @Operation(summary = "Export all transactions to CSV")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        List<Transaction> all = transactionService.getAllExportData();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Time,Amount,Location,Device,Risk Score,Risk Level,Status,Flag Reason,Reviewed By\n");
        
        for (Transaction t : all) {
            csv.append(t.getId() != null ? t.getId().toString() : "").append(",")
               .append(t.getCreatedAt() != null ? t.getCreatedAt().toString() : "").append(",")
               .append(t.getAmount() != null ? t.getAmount().toString() : "").append(",")
               .append(t.getLocation() != null ? escapeCsv(t.getLocation()) : "").append(",")
               .append(t.getDevice() != null ? t.getDevice() : "").append(",")
               .append(t.getFraudProbability() != null ? t.getFraudProbability().toString() : "").append(",")
               .append(t.getRiskLevel() != null ? t.getRiskLevel() : "").append(",")
               .append(t.getStatus() != null ? t.getStatus() : "").append(",")
               .append(t.getFlagReason() != null ? escapeCsv(t.getFlagReason()) : "").append(",")
               .append(t.getReviewedBy() != null ? t.getReviewedBy() : "").append("\n");
        }

        byte[] data = csv.toString().getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "transactions_export.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
    
    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
