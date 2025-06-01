package com.dengas.devtimetracker.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Embeddable
public class DailyStats {
    @NotNull(message = "Coding time must not be null")
    private Long codingTime;

    @NotNull(message = "Open time must not be null")
    private Long openTime;

    public DailyStats(Long codingTime, Long openTime) {
        this.codingTime = codingTime;
        this.openTime = openTime;
    }

    public DailyStats() {
    }
}