package com.fraud.controller;

import com.fraud.model.Transaction;
import com.fraud.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction simulation and management")
public class TransactionController {

    private final TransactionService transactionService;
    private final Random random = new Random();

    @Operation(summary = "Get paginated transactions")
    @GetMapping
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(transactionService.getAllTransactions(pageRequest));
    }

    @Operation(summary = "Get high-risk flagged transactions")
    @GetMapping("/flagged")
    public ResponseEntity<List<Transaction>> getFlaggedTransactions() {
        return ResponseEntity.ok(transactionService.getFlaggedTransactions());
    }

    @Operation(summary = "Get flagged transactions pending review")
    @GetMapping("/pending-review")
    public ResponseEntity<List<Transaction>> getPendingReviewTransactions() {
        return ResponseEntity.ok(transactionService.getPendingFlaggedTransactions());
    }

    @Operation(summary = "Approve a flagged transaction")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Transaction> approveTransaction(@PathVariable UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(transactionService.reviewTransaction(id, "APPROVED", username));
    }

    @Operation(summary = "Reject a flagged transaction")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Transaction> rejectTransaction(@PathVariable UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(transactionService.reviewTransaction(id, "REJECTED", username));
    }

    @Operation(summary = "Simulate a random new transaction")
    @PostMapping("/simulate")
    public ResponseEntity<Transaction> simulateTransaction() {
        String[] locations = {"New York", "London", "Tokyo", "Paris", "Berlin"};
        String[] devices = {"mobile", "desktop", "tablet"};

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        
        // Random amount between 10 and 10000
        double amount = 10 + (10000 - 10) * random.nextDouble();
        transaction.setAmount(BigDecimal.valueOf(amount));
        
        transaction.setLocation(locations[random.nextInt(locations.length)]);
        transaction.setDevice(devices[random.nextInt(devices.length)]);
        transaction.setHourOfDay(LocalDateTime.now().getHour());
        
        // Random chance of foreign location
        transaction.setIsForeignLocation(random.nextDouble() < 0.1);
        
        // Random transactions last hour
        transaction.setTransactionsLastHour(random.nextInt(5));
        
        // Overrides to test new rules:
        // 10% chance to test Rule 1 (Amount > 9000)
        if (random.nextDouble() < 0.10) transaction.setAmount(BigDecimal.valueOf(9500 + random.nextDouble() * 1000));
        // 10% chance to test Rule 2 (High velocity)
        if (random.nextDouble() < 0.10) transaction.setTransactionsLastHour(6 + random.nextInt(5));
        // 10% chance to test Rule 3 (Foreign + night)
        if (random.nextDouble() < 0.10) { transaction.setIsForeignLocation(true); transaction.setHourOfDay(random.nextInt(6)); }

        transaction.setUserId("user-" + random.nextInt(1000));
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction savedTransaction = transactionService.processTransaction(transaction);
        return ResponseEntity.ok(savedTransaction);
    }
}
