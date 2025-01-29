package com.alquiler.car_rent.controllers.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.controllers.SmsApi;
import com.alquiler.car_rent.service.SmsService;

@RestController
public class SmsController implements SmsApi {
	
	private final SmsService smsService;
	
	public SmsController(SmsService smsService) {
		this.smsService = smsService;
		
	}

	@Override
	public ResponseEntity<String> sendSms(String to, String message) {
		smsService.sendSms(to, message);
		return ResponseEntity.ok("Sms Enviado con exito a " + to);
	}

}
