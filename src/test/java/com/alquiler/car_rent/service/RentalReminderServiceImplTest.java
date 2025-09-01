package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.entities.Customer;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.impl.AdminAlertServiceImpl;
import com.alquiler.car_rent.service.impl.RentalReminderServiceImpl;
import com.alquiler.car_rent.service.impl.SmsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalReminderServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private SmsServiceImpl smsService;

    @Mock
    private AdminAlertServiceImpl adminAlertService;

    @InjectMocks
    private RentalReminderServiceImpl rentalReminderService;

    private Rental rental;
    private Customer customer;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("John Doe");
        customer.setPhone("+1234567890");

        vehicle = new Vehicle();
        vehicle.setModel("Corolla");
        vehicle.setBrand("Toyota");
        vehicle.setPlate("ABC-123");

        rental = new Rental();
        rental.setCustomer(customer);
        rental.setVehicle(vehicle);
        rental.setRentalStatus(RentalStatus.ACTIVE);
    }

    @Test
    void sendReminders_shouldSendSmsAndAlert_whenRentalEndsWithinOneHour() {
        // Arrange
        rental.setEndDate(LocalDateTime.now().plusMinutes(30));
        when(rentalRepository.findByRentalStatus(RentalStatus.ACTIVE)).thenReturn(List.of(rental));

        // Act
        rentalReminderService.sendReminders();

        // Assert
        verify(smsService, times(1)).sendSms(eq(customer.getPhone()), anyString());
        verify(adminAlertService, times(1)).sendRentalEndingAlert(anyString());
    }

    @Test
    void sendReminders_shouldNotSendAnything_whenRentalEndsAfterOneHour() {
        // Arrange
        rental.setEndDate(LocalDateTime.now().plusHours(2));
        when(rentalRepository.findByRentalStatus(RentalStatus.ACTIVE)).thenReturn(List.of(rental));

        // Act
        rentalReminderService.sendReminders();

        // Assert
        verify(smsService, never()).sendSms(anyString(), anyString());
        verify(adminAlertService, never()).sendRentalEndingAlert(anyString());
    }

    @Test
    void sendReminders_shouldNotSendAnything_whenNoRelevantRentalsAreFound() {
        // Arrange: This covers cases where the rental already ended, is not active, or the list is empty.
        // In all these scenarios, the list of rentals to remind will be empty.
        when(rentalRepository.findByRentalStatus(RentalStatus.ACTIVE)).thenReturn(Collections.emptyList());

        // Act
        rentalReminderService.sendReminders();

        // Assert
        verify(smsService, never()).sendSms(anyString(), anyString());
        verify(adminAlertService, never()).sendRentalEndingAlert(anyString());
    }
}