package com.alquiler.car_rent.service.provider;

import com.alquiler.car_rent.config.TwilioConfigProperties;
import com.alquiler.car_rent.service.SmsProvider;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("twilio")
public class TwilioSmsProvider implements SmsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioSmsProvider.class);

    private final TwilioConfigProperties twilioConfig;

    public TwilioSmsProvider(TwilioConfigProperties twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    @PostConstruct
    public void init() {
        if (twilioConfig.isEnabled()) {
            if (twilioConfig.getAccountSid() == null || twilioConfig.getAuthToken() == null) {
                LOGGER.warn("Twilio está habilitado pero las credenciales (SID o Token) no están configuradas.");
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
            LOGGER.debug("Intento de envío de SMS con Twilio mientras el servicio está deshabilitado. Ignorando.");
            return;
        }

        try {
            Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(twilioConfig.getPhoneNumber()),
                text
            ).create();
            LOGGER.info("SMS enviado exitosamente con Twilio al número: {}", to);
        } catch (ApiException e) {
            LOGGER.error("Error al enviar SMS con Twilio al número {}: {} - Código de error: {}", to, e.getMessage(), e.getCode(), e);
            // Aquí se podría lanzar una excepción personalizada
        }
    }

    @Override
    public String getProviderName() {
        return "twilio";
    }
}
