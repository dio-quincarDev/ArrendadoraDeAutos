package com.alquiler.car_rent.service;

import com.alquiler.car_rent.config.SmsProviderConfig;
import com.alquiler.car_rent.service.SmsProvider;
import com.alquiler.car_rent.service.impl.SmsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceImplTest {

    @Mock
    private SmsProviderConfig providerConfig;

    @Mock
    private SmsProvider twilioProvider;

    @Mock
    private SmsProvider vonageProvider;

    private SmsServiceImpl smsService;

    @Test
    void sendSms_shouldUseTwilio_whenTwilioIsTheActiveProvider() {
        // Arrange
        when(providerConfig.getProvider()).thenReturn("twilio");
        when(twilioProvider.getProviderName()).thenReturn("twilio");
        when(vonageProvider.getProviderName()).thenReturn("vonage");

        // Act
        smsService = new SmsServiceImpl(providerConfig, List.of(twilioProvider, vonageProvider));
        String toNumber = "+123456789";
        String message = "Mensaje de prueba";
        smsService.sendSms(toNumber, message);

        // Assert
        verify(twilioProvider, times(1)).sendSms(toNumber, message);
        verify(vonageProvider, never()).sendSms(anyString(), anyString());
    }

    @Test
    void sendSms_shouldUseVonage_whenVonageIsTheActiveProvider() {
        // Arrange
        when(providerConfig.getProvider()).thenReturn("vonage");
        when(twilioProvider.getProviderName()).thenReturn("twilio");
        when(vonageProvider.getProviderName()).thenReturn("vonage");

        // Act
        smsService = new SmsServiceImpl(providerConfig, List.of(twilioProvider, vonageProvider));
        String toNumber = "+987654321";
        String message = "Otro mensaje";
        smsService.sendSms(toNumber, message);

        // Assert
        verify(vonageProvider, times(1)).sendSms(toNumber, message);
        verify(twilioProvider, never()).sendSms(anyString(), anyString());
    }

    @Test
    void sendSms_shouldDoNothing_whenNoProviderIsFound() {
        // Arrange
        when(providerConfig.getProvider()).thenReturn("invalid-provider");
        when(twilioProvider.getProviderName()).thenReturn("twilio");
        when(vonageProvider.getProviderName()).thenReturn("vonage");

        // Act
        smsService = new SmsServiceImpl(providerConfig, List.of(twilioProvider, vonageProvider));
        smsService.sendSms("any-number", "any-message");

        // Assert
        verify(twilioProvider, never()).sendSms(anyString(), anyString());
        verify(vonageProvider, never()).sendSms(anyString(), anyString());
    }
}
