package com.example.carins.scheduling;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.model.PolicyExpiryLog;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.PolicyExpiryLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Component
public class PolicyExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PolicyExpiryScheduler.class);

    private final InsurancePolicyRepository policyRepo;
    private final PolicyExpiryLogRepository expiryLogRepo;
    private final Clock clock;

    public PolicyExpiryScheduler(InsurancePolicyRepository policyRepo,
                                 PolicyExpiryLogRepository expiryLogRepo,
                                 Clock clock) {
        this.policyRepo = policyRepo;
        this.expiryLogRepo = expiryLogRepo;
        this.clock = clock;
    }

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void logExpiredPolicies() {
        LocalDate today = LocalDate.now(clock);
        LocalTime nowTime = LocalTime.now(clock);

        if (nowTime.isAfter(LocalTime.of(0, 59, 59)) && !nowTime.equals(LocalTime.MIDNIGHT)) {
            return;
        }

        for (InsurancePolicy p : policyRepo.findByEndDate(today)) {
            if (!expiryLogRepo.existsByPolicyId(p.getId())) {
                log.info("Policy {} for car {} expired on {}", p.getId(),
                        p.getCar().getId(), p.getEndDate());
                expiryLogRepo.save(new PolicyExpiryLog(p.getId(), Instant.now(clock)));
            }
        }
    }
}