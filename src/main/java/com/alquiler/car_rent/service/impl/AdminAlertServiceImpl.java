package com.alquiler.car_rent.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.service.AdminAlertService;

@Service
public class AdminAlertServiceImpl implements AdminAlertService {

    private final SimpMessagingTemplate messagingTemplate;

    public AdminAlertServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendRentalEndingAlert(String message) {
        messagingTemplate.convertAndSend("/topic/rental-alerts", message);
    }
}
