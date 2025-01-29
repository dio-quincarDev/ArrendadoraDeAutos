package com.alquiler.car_rent.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.SmsService;

@Service
public class RentalReminderServiceImpl {
	private final RentalRepository rentalRepository;
	private final SmsService smsService;
	
	public RentalReminderServiceImpl(RentalRepository rentalRepository, SmsService smsService) {
		this.rentalRepository = rentalRepository;
		this.smsService = smsService;
	}
	
	@Scheduled(fixedRate = 90000)
	public void sendReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourFromNow = now.plusHours(1);
		
		List<Rental>rentalToRemind = rentalRepository.findByRentalStatusAndEndDateBetween(
				RentalStatus.ACTIVE, now, oneHourFromNow);
		
		for (Rental rental : rentalToRemind) {
			String message = "Hola" + rental.getCustomer().getName() + 
					", recuerde que debe devolver el vehiculo' " + rental.getVehicle().getModel() + 
					"' a mas tardar" + rental.getEndDate() + "Si ya lo devolvio, ignore este sms";
			smsService.sendSms(rental.getCustomer().getPhone(), message);
		}
	}

}
