package com.alquiler.car_rent.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import jakarta.annotation.PostConstruct;

@Service
public class SmsServiceImpl implements SmsService{

	@Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;
    
    
    @PostConstruct
    public void init() {
    	 if (accountSid == null || authToken == null || fromPhoneNumber == null) {
             throw new IllegalStateException("Las credenciales de Twilio no están configuradas correctamente.");
         }
        Twilio.init(accountSid, authToken);
    }
 

	@Override
	public void sendSms(String to, String message) {
		
		Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(fromPhoneNumber),
                message
        ).create();
	}

}
