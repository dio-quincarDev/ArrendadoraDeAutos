package com.alquiler.car_rent.service;

import com.alquiler.car_rent.service.impl.AdminAlertServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AdminAlertServiceImplTest {
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AdminAlertServiceImpl adminAlertServiceImpl;

    @Test
    void sendRentalEndingAlert_ShouldSendMessageToCorrectTopic(){

        String testMessage = "Alerta de Prueba";
        String expectedTopic = "/topic/rental-alerts";

        adminAlertServiceImpl.sendRentalEndingAlert(testMessage);

        verify(messagingTemplate, times(1)).convertAndSend(expectedTopic, testMessage);

    }



}
