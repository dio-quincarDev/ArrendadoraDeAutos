package com.alquiler.car_rent.service.impl;

import org.springframework.stereotype.Service;
import com.alquiler.car_rent.config.TwilioConfigProperties;
import com.alquiler.car_rent.service.SmsService;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final TwilioConfigProperties twilioConfig;

    public SmsServiceImpl(TwilioConfigProperties twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    @PostConstruct
    public void init() {
        if (twilioConfig.isEnabled()) {
            if (twilioConfig.getAccountSid() == null || twilioConfig.getAuthToken() == null) {
                LOGGER.warn("Twilio está habilitado pero las credenciales (SID o Token) no están configuradas. No se podrán enviar SMS.");
            } else {
                Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
                LOGGER.info("Servicio de Twilio inicializado.");
            }
        } else {
            LOGGER.info("El servicio de Twilio está deshabilitado por configuración.");
        }
    }

    @Override
    public void sendSms(String to, String text) {
        if (!twilioConfig.isEnabled()) {
            LOGGER.debug("Intento de envío de SMS mientras el servicio está deshabilitado. Ignorando.");
            return;
        }

        try {
            Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(twilioConfig.getPhoneNumber()),
                text
            ).create();
            LOGGER.info("SMS enviado exitosamente al número: {}", to);
        } catch (ApiException e) {
            LOGGER.error("Error al enviar SMS al número {}: {} - Código de error: {}", to, e.getMessage(), e.getCode(), e);
            // La excepción no se relanza para no detener el proceso principal (ej. recordatorios)
        }
    }
}
