package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleType;
import com.alquiler.car_rent.service.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingServiceImpl implements PricingService {

    @Override
    public BigDecimal calculateDailyRate(VehicleType vehicleType, PricingTier chosenPricingTier) {
        if (vehicleType == null || chosenPricingTier == null) {
            throw new IllegalStateException("El tipo de vehículo o el nivel de precios elegido no pueden ser nulos.");
        }

        switch (chosenPricingTier) {
            case PROMOTIONAL:
                return vehicleType.getPromotionalRate();
            case PREMIUM:
                return vehicleType.getPremiumRate();
            case STANDARD:
            default:
                return vehicleType.getStandardRate();
        }
    }
}
