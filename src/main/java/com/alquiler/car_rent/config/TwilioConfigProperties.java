package com.alquiler.car_rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "twilio")
@Data
public class TwilioConfigProperties {
    private String accountSid;
    private String authToken;
    private String phoneNumber;
    private boolean enabled = true; // Habilitado por defecto
}
