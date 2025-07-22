package com.alquiler.car_rent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "vonage")
@Data
public class VonageConfigProperties {
    private String apiKey;
    private String apiSecret;
    private String fromNumber;
}
