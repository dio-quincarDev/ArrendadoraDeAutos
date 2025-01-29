package com.alquiler.car_rent.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/v1/sms")
public interface SmsApi {
	@PostMapping("/send")
	ResponseEntity<String>sendSms(@RequestParam String to, @RequestParam String message);

}
