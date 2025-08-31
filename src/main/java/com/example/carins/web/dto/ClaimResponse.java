package com.example.carins.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ClaimResponse {
    private Long id;
    private Long carId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate claimDate;

    private String description;
    private BigDecimal amount;

    public ClaimResponse(Long id, Long carId, LocalDate claimDate, String description, BigDecimal amount) {
        this.id = id;
        this.carId = carId;
        this.claimDate = claimDate;
        this.description = description;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public Long getCarId() { return carId; }
    public LocalDate getClaimDate() { return claimDate; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
}