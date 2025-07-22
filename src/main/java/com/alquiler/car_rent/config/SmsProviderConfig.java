package com.alquiler.car_rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "sms")
@Data
public class SmsProviderConfig {
    private String provider = "vonage"; // 'twilio' por defecto para no romper la funcionalidad existente
}
