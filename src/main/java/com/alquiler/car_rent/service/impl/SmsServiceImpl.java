package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.config.SmsProviderConfig;
import com.alquiler.car_rent.service.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SmsServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final SmsProvider activeProvider;

    public SmsServiceImpl(SmsProviderConfig providerConfig, List<SmsProvider> smsProviders) {
        Map<String, SmsProvider> providerMap = smsProviders.stream()
                .collect(Collectors.toMap(SmsProvider::getProviderName, Function.identity()));

        String providerName = providerConfig.getProvider();
        this.activeProvider = providerMap.get(providerName);

        if (this.activeProvider != null) {
            LOGGER.info("Proveedor de SMS activo: {}", this.activeProvider.getProviderName());
        } else {
            LOGGER.error("No se encontró un proveedor de SMS para el nombre: {}. El envío de SMS no funcionará.", providerName);
            // Considera lanzar una excepción aquí si el envío de SMS es crítico
        }
    }

    public void sendSms(String to, String text) {
        if (activeProvider == null) {
            LOGGER.warn("No hay un proveedor de SMS activo. No se puede enviar el mensaje.");
            return;
        }
        activeProvider.sendSms(to, text);
    }
}
