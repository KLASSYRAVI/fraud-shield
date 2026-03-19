package com.fraud.repository;

import com.fraud.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByFlaggedTrueOrderByCreatedAtDesc();
    
    List<Transaction> findByFlaggedTrueAndStatusOrderByCreatedAtDesc(String status);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.flagged = true")
    long countFlaggedTransactions();
    
    @Query("SELECT AVG(t.fraudProbability) FROM Transaction t")
    Double getAverageRiskScore();
    
    List<Transaction> findByCreatedAtAfter(LocalDateTime date);
}
