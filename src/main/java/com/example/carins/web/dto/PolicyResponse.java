package com.example.carins.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class PolicyResponse {
    private Long id;
    private Long carId;
    private String provider;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    public PolicyResponse(Long id, Long carId, String provider,
                          LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.carId = carId;
        this.provider = provider;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public Long getCarId() {
        return carId;
    }

    public String getProvider() {
        return provider;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
