package com.alquiler.car_rent.commons.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum VehicleType {

    // Tarifa estandar 80.00
    PICKUP(new BigDecimal("72.00"), new BigDecimal("80.00"), new BigDecimal("92.00")),
    // Tarifa estandar 75.00
    SUV(new BigDecimal("67.50"), new BigDecimal("75.00"), new BigDecimal("86.25")),
    // Tarifa estandar 40.00
    SEDAN(new BigDecimal("36.00"), new BigDecimal("40.00"), new BigDecimal("46.00")),
    // Tarifa estandar 35.00
    HATCHBACK(new BigDecimal("31.50"), new BigDecimal("35.00"), new BigDecimal("40.25"));

    private final BigDecimal promotionalRate;
    private final BigDecimal standardRate;
    private final BigDecimal premiumRate;

    VehicleType(BigDecimal promotionalRate, BigDecimal standardRate, BigDecimal premiumRate) {
        this.promotionalRate = promotionalRate.setScale(2, RoundingMode.HALF_UP);
        this.standardRate = standardRate.setScale(2, RoundingMode.HALF_UP);
        this.premiumRate = premiumRate.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPromotionalRate() { return promotionalRate; }
    public BigDecimal getStandardRate() { return standardRate; }
    public BigDecimal getPremiumRate() { return premiumRate; }
}
