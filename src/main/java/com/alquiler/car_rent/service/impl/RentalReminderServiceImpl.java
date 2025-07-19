package com.alquiler.car_rent.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.AdminAlertService;
import com.alquiler.car_rent.service.SmsService;

@Service
public class RentalReminderServiceImpl {
	private final RentalRepository rentalRepository;
	private final SmsService smsService;
	private final AdminAlertService adminAlertService;

	public RentalReminderServiceImpl(RentalRepository rentalRepository, SmsService smsService, AdminAlertService adminAlertService) {
		this.rentalRepository = rentalRepository;
		this.smsService = smsService;
		this.adminAlertService = adminAlertService;
	}

	@Scheduled(fixedRate = 90000)
	public void sendReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourFromNow = now.plusHours(1);

		List<Rental> rentalsActive = rentalRepository.findByRentalStatus(RentalStatus.ACTIVE);
		List<Rental> rentalsToRemind = rentalsActive.stream()
				.filter(rental -> !rental.getEndDate().isBefore(now) && rental.getEndDate().isBefore(oneHourFromNow))
				.toList();

		for (Rental rental : rentalsToRemind) {
			String messageSms = "Hola " + rental.getCustomer().getName() +
					", recuerde que debe devolver el vehiculo '" + rental.getVehicle().getModel() +
					"' a mas tardar " + rental.getEndDate() + ". Si ya lo devolvio, ignore este sms";
			smsService.sendSms(rental.getCustomer().getPhone(), messageSms);
			
			String messageWs = "El alquiler del vehiculo " + rental.getVehicle().getBrand() + " " + rental.getVehicle().getModel() +
                    " (Placa: " + rental.getVehicle().getPlate() + ") rentado por " + rental.getCustomer().getName() +
                    " esta proximo a vencer a las " + rental.getEndDate().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
			adminAlertService.sendRentalEndingAlert(messageWs);
		}
	}
}
