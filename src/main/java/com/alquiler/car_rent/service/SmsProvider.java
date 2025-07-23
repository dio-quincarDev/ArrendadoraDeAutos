package com.alquiler.car_rent.service;

public interface SmsProvider {
    void sendSms(String to, String message);
    String getProviderName();
}
