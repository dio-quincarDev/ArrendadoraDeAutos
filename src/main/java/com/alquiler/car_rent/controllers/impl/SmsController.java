package com.alquiler.car_rent.controllers.impl;

import com.alquiler.car_rent.service.impl.SmsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.alquiler.car_rent.controllers.SmsApi;

@RestController
public class SmsController implements SmsApi {
	
	private final SmsServiceImpl smsService;
	
	public SmsController(SmsServiceImpl smsService) {
		this.smsService = smsService;
	}

	@Override
	public ResponseEntity<String> sendSms(String to, String message) {
		smsService.sendSms(to, message);
		return ResponseEntity.ok("Solicitud de envío de SMS procesada para " + to);
	}

}
