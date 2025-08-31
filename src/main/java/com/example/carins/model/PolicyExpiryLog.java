package com.example.carins.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "policy_expiry_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_policy_expiry_log_policy", columnNames = "policy_id")
})
public class PolicyExpiryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", nullable = false, unique = true)
    private Long policyId;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;

    protected PolicyExpiryLog() {}

    public PolicyExpiryLog(Long policyId, Instant loggedAt) {
        this.policyId = policyId;
        this.loggedAt = loggedAt;
    }

    public Long getId() { return id; }
    public Long getPolicyId() { return policyId; }
    public Instant getLoggedAt() { return loggedAt; }
}