package com.example.carins.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.Map;

public class HistoryEvent {
    private String type; // "POLICY" or "CLAIM"

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String details;

    private Map<String, Object> extra;

    public HistoryEvent(String type, LocalDate date, String details, Map<String, Object> extra) {
        this.type = type;
        this.date = date;
        this.details = details;
        this.extra = extra;
    }

    public String getType() { return type; }
    public LocalDate getDate() { return date; }
    public String getDetails() { return details; }
    public Map<String, Object> getExtra() { return extra; }
}