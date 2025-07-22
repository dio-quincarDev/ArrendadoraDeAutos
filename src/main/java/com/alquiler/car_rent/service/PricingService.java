package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleType;
import java.math.BigDecimal;

/**
 * Servicio para centralizar toda la lógica de cálculo de precios.
 */
public interface PricingService {

    /**
     * Calcula la tarifa de alquiler diaria para un vehículo específico basado en su
     * tipo y el nivel de precios elegido.
     *
     * @param vehicleType El tipo de vehículo (ej. SEDAN, SUV).
     * @param chosenPricingTier El nivel de precios elegido para la renta (ej. PROMOTIONAL, STANDARD).
     * @return La tarifa diaria calculada como un BigDecimal.
     * @throws IllegalStateException si el tipo de vehículo o el nivel de precios no son válidos.
     */
    BigDecimal calculateDailyRate(VehicleType vehicleType, PricingTier chosenPricingTier);

}
