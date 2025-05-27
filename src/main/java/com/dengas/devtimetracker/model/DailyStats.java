package com.dengas.devtimetracker.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class DailyStats {
    private long codingTime;
    private long openTime;
}