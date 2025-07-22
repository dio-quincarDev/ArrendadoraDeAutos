package com.alquiler.car_rent.service.provider;

import com.alquiler.car_rent.config.VonageConfigProperties;
import com.alquiler.car_rent.service.SmsProvider;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("vonage")
public class VonageSmsProvider implements SmsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(VonageSmsProvider.class);

    private final VonageConfigProperties vonageConfig;
    private VonageClient client;

    public VonageSmsProvider(VonageConfigProperties vonageConfig) {
        this.vonageConfig = vonageConfig;
    }

    @PostConstruct
    public void init() {
        if (vonageConfig.getApiKey() != null && vonageConfig.getApiSecret() != null) {
            this.client = VonageClient.builder()
                .apiKey(vonageConfig.getApiKey())
                .apiSecret(vonageConfig.getApiSecret())
                .build();
            LOGGER.info("Servicio de Vonage inicializado.");
        } else {
            LOGGER.warn("Vonage está seleccionado como proveedor pero las credenciales (API Key o Secret) no están configuradas.");
        }
    }

    @Override
    public void sendSms(String to, String message) {
        if (client == null) {
            LOGGER.error("No se puede enviar SMS con Vonage porque el cliente no está inicializado.");
            return;
        }

        try {
            TextMessage textMessage = new TextMessage(
                vonageConfig.getFromNumber(), // Alphanumeric Sender ID
                to,
                message
            );

            SmsSubmissionResponse response = client.getSmsClient().submitMessage(textMessage);

            if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
                LOGGER.info("SMS enviado exitosamente con Vonage al número: {}", to);
            } else {
                LOGGER.error("Error al enviar SMS con Vonage. Estado: {} - Detalle: {}", 
                    response.getMessages().get(0).getStatus(), 
                    response.getMessages().get(0).getErrorText());
            }
        } catch (Exception e) {
            LOGGER.error("Excepción al enviar SMS con Vonage al número {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "vonage";
    }
}
