package com.fraud.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 100)
    private String location;

    @Column(length = 50)
    private String device;

    @Column(name = "hour_of_day")
    private Integer hourOfDay;

    @Column(name = "is_foreign_location")
    private Boolean isForeignLocation;

    @Column(name = "transactions_last_hour")
    private Integer transactionsLastHour;

    @Column(name = "fraud_probability", precision = 5, scale = 4)
    private BigDecimal fraudProbability;

    @Column(name = "risk_level", length = 10)
    private String riskLevel;

    private Boolean flagged = false;

    @Column(length = 500, name = "flag_reason")
    private String flagReason;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
