package com.fraud.service;

import com.fraud.model.Transaction;
import com.fraud.repository.TransactionRepository;
import com.fraud.dto.RiskScore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudScoringService fraudScoringService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditLogService auditLogService;

    @Transactional
    public Transaction processTransaction(Transaction transaction) {
        RiskScore score = fraudScoringService.scoreTransaction(transaction);
        
        double fraudProb = score.getFraud_probability().doubleValue();
        String currentRiskLevel = score.getRisk_level();
        boolean overrideFlag = false;

        // Rule 3: foreign location + night (0-5)
        if (Boolean.TRUE.equals(transaction.getIsForeignLocation()) && transaction.getHourOfDay() != null && transaction.getHourOfDay() >= 0 && transaction.getHourOfDay() <= 5) {
            fraudProb += 0.15;
            if (fraudProb > 1.0) fraudProb = 1.0;
        }
        
        // Recalculate level based on updated prob
        if (fraudProb > 0.7) currentRiskLevel = "HIGH";
        else if (fraudProb > 0.3) currentRiskLevel = "MEDIUM";
        else currentRiskLevel = "LOW";

        // Rule 2: transactions > 5
        if (transaction.getTransactionsLastHour() != null && transaction.getTransactionsLastHour() > 5) {
            if ("LOW".equals(currentRiskLevel)) currentRiskLevel = "MEDIUM";
            else if ("MEDIUM".equals(currentRiskLevel)) currentRiskLevel = "HIGH";
        }

        // Rule 1: amount > 9000
        if (transaction.getAmount() != null && transaction.getAmount().doubleValue() > 9000) {
            currentRiskLevel = "HIGH";
            overrideFlag = true;
        }

        boolean flagged = overrideFlag || "HIGH".equals(currentRiskLevel);

        transaction.setFraudProbability(BigDecimal.valueOf(fraudProb));
        transaction.setRiskLevel(currentRiskLevel);
        transaction.setFlagged(flagged);
        
        if (flagged) {
            transaction.setFlagReason(getRuleExplanation(transaction));
        }

        Transaction saved = transactionRepository.save(transaction);
        
        auditLogService.log("TRANSACTION_CREATED", "Transaction", saved.getId().toString(), saved.getUserId(), "Simulated transaction");
        if (flagged) {
            auditLogService.log("HIGH_RISK_DETECTED", "Transaction", saved.getId().toString(), "SYSTEM", saved.getFlagReason());
        }

        messagingTemplate.convertAndSend("/topic/transactions", saved);

        return saved;
    }

    private String getRuleExplanation(Transaction tx) {
        List<String> reasons = new ArrayList<>();
        if (tx.getAmount() != null && tx.getAmount().doubleValue() > 9000) {
            reasons.add("Large amount");
        }
        if (tx.getTransactionsLastHour() != null && tx.getTransactionsLastHour() > 5) {
            reasons.add("High velocity");
        }
        if (Boolean.TRUE.equals(tx.getIsForeignLocation()) && tx.getHourOfDay() != null && tx.getHourOfDay() >= 0 && tx.getHourOfDay() <= 5) {
            reasons.add("Foreign location at night");
        }
        return reasons.isEmpty() ? "High ML Risk Score" : String.join(" + ", reasons);
    }

    @Transactional
    public Transaction reviewTransaction(UUID id, String status, String username) {
        Transaction tx = transactionRepository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));
        tx.setStatus(status);
        tx.setReviewedBy(username);
        tx.setReviewedAt(LocalDateTime.now());
        Transaction updated = transactionRepository.save(tx);

        auditLogService.log("TRANSACTION_REVIEWED", "Transaction", tx.getId().toString(), username, "Status changed to " + status);
        
        // Broadcast update to dashboard to refresh UI automatically
        messagingTemplate.convertAndSend("/topic/transactions", updated);
        
        return updated;
    }

    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }
    
    public List<Transaction> getPendingFlaggedTransactions() {
        return transactionRepository.findByFlaggedTrueAndStatusOrderByCreatedAtDesc("PENDING");
    }

    public List<Transaction> getFlaggedTransactions() {
        return transactionRepository.findByFlaggedTrueOrderByCreatedAtDesc();
    }
    
    public long getTotalTransactions() {
        return transactionRepository.count();
    }
    
    public long getFlaggedCount() {
        return transactionRepository.countFlaggedTransactions();
    }
    
    public Double getAverageRiskScore() {
        Double avg = transactionRepository.getAverageRiskScore();
        return avg != null ? avg : 0.0;
    }

    public List<Transaction> getTransactionsLast24Hours() {
        return transactionRepository.findByCreatedAtAfter(LocalDateTime.now().minusHours(24));
    }
    
    public List<Transaction> getAllExportData() {
        return transactionRepository.findAll();
    }
}
