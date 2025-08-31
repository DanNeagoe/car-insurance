package com.example.carins.service;

import com.example.carins.model.PolicyExpiryLog;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.PolicyExpiryLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class PolicyExpiryService {
    private final InsurancePolicyRepository repo;
    private final PolicyExpiryLogRepository logRepo;

    public PolicyExpiryService(InsurancePolicyRepository repo, PolicyExpiryLogRepository logRepo) {
        this.repo = repo;
        this.logRepo = logRepo;
    }

    @Transactional
    public int logExpiredPolicies(LocalDate today) {
        var expired = repo.findExpiredAsOf(today);
        int n = 0;
        for (var p : expired) {
            if (!logRepo.existsByPolicyId(p.getId())) {
                logRepo.save(new PolicyExpiryLog(p.getId(), Instant.now()));
                n++;
            }
        }
        return n;
    }

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onStartup() {
        logExpiredPolicies(LocalDate.now());
    }

    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 60 * 1000)
    public void scheduledCheck() {
        logExpiredPolicies(LocalDate.now());
    }
}