package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.entities.Customer;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.enums.VehicleType;
import com.alquiler.car_rent.commons.mappers.RentalMapper;
import com.alquiler.car_rent.exceptions.BadRequestException;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.impl.RentalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private PricingService pricingService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private Rental rental;
    private RentalDto rentalDto;
    private Vehicle vehicle;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // Configuración de datos de prueba comunes
        customer = new Customer();
        customer.setId(1L);

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setVehicleType(VehicleType.SEDAN);

        rentalDto = new RentalDto();
        rentalDto.setCustomerId(1L);
        rentalDto.setVehicleId(1L);
        rentalDto.setStartDate(LocalDateTime.now().plusDays(1));
        rentalDto.setEndDate(LocalDateTime.now().plusDays(3));
        rentalDto.setChosenPricingTier(PricingTier.STANDARD);

        rental = new Rental();
        rental.setId(1L);
        rental.setCustomer(customer);
        rental.setVehicle(vehicle);
        rental.setStartDate(rentalDto.getStartDate());
        rental.setEndDate(rentalDto.getEndDate());
        rental.setChosenPricingTier(PricingTier.STANDARD);
    }

    // Caso de éxito: Crear un alquiler válido.
    @Test
    void createRental_shouldSucceed_whenDataIsValid() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(pricingService.calculateDailyRate(any(VehicleType.class), any(PricingTier.class))).thenReturn(new BigDecimal("100"));
        when(rentalMapper.dtoToRental(any(RentalDto.class))).thenReturn(rental);
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.rentalToDto(any(Rental.class))).thenReturn(rentalDto);

        // Act
        RentalDto result = rentalService.createRental(rentalDto);

        // Assert
        assertNotNull(result);
        // Verifica que el estado del vehículo se actualizó a RENTED.
        verify(vehicleRepository, times(1)).save(vehicle);
        assertEquals(VehicleStatus.RENTED, vehicle.getStatus());
        // Verifica que el alquiler se guardó.
        verify(rentalRepository, times(1)).save(rental);
    }

    // Caso borde: La fecha de inicio es posterior a la fecha de fin.
    @Test
    void createRental_shouldThrowBadRequestException_whenStartDateIsAfterEndDate() {
        // Arrange
        rentalDto.setStartDate(LocalDateTime.now().plusDays(3));
        rentalDto.setEndDate(LocalDateTime.now().plusDays(1));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            rentalService.createRental(rentalDto);
        });
        assertEquals("La fecha de inicio no puede ser posterior a la fecha de fin.", exception.getMessage());
    }

    // Caso borde: El vehículo a alquilar no está disponible.
    @Test
    void createRental_shouldThrowBadRequestException_whenVehicleIsNotAvailable() {
        // Arrange
        vehicle.setStatus(VehicleStatus.MAINTENANCE); // Estado no disponible
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(rentalMapper.dtoToRental(any(RentalDto.class))).thenReturn(rental);


        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            rentalService.createRental(rentalDto);
        });
        assertEquals("El vehiculo no está disponible para alquiler", exception.getMessage());
    }

    // Caso borde: El cliente especificado no existe.
    @Test
    void createRental_shouldThrowNotFoundException_whenCustomerDoesNotExist() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        when(rentalMapper.dtoToRental(any(RentalDto.class))).thenReturn(rental);


        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            rentalService.createRental(rentalDto);
        });
        assertEquals("Cliente No Encontrado por ID: 1", exception.getMessage());
    }

    // Caso borde: El nivel de precios (pricing tier) no se especifica.
    @Test
    void createRental_shouldThrowBadRequestException_whenPricingTierIsNull() {
        // Arrange
        rentalDto.setChosenPricingTier(null);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(rentalMapper.dtoToRental(any(RentalDto.class))).thenReturn(rental);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            rentalService.createRental(rentalDto);
        });
        assertEquals("El nivel de precios elegido (chosenPricingTier) es requerido.", exception.getMessage());
    }

    // Caso de éxito: Cancelar un alquiler existente.
    @Test
    void cancelRental_shouldSucceed_whenRentalExists() {
        // Arrange
        rental.setRentalStatus(RentalStatus.ACTIVE);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.rentalToDto(any(Rental.class))).thenReturn(rentalDto);

        // Act
        rentalService.cancelRental(1L);

        // Assert
        // Verifica que el estado del alquiler se cambió a CANCELLED.
        assertEquals(RentalStatus.CANCELLED, rental.getRentalStatus());
        // Verifica que el precio total se restableció a cero.
        assertEquals(BigDecimal.ZERO, rental.getTotalPrice());
        // Verifica que el estado del vehículo se restauró a AVAILABLE.
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        verify(vehicleRepository, times(1)).save(vehicle);
        verify(rentalRepository, times(1)).save(rental);
    }

    // Caso borde: Intentar cancelar un alquiler que no existe.
    @Test
    void cancelRental_shouldThrowNotFoundException_whenRentalDoesNotExist() {
        // Arrange
        when(rentalRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            rentalService.cancelRental(1L);
        });
        assertEquals("Alquiler no encontrado con ID: 1", exception.getMessage());
    }

    // Caso de éxito: Actualizar un alquiler con fechas y recalcular el precio.
    @Test
    void updateRental_shouldRecalculatePrice_whenDatesChange() {
        // Arrange
        rentalDto.setEndDate(LocalDateTime.now().plusDays(5)); // Se extienden las fechas
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(pricingService.calculateDailyRate(any(VehicleType.class), any(PricingTier.class))).thenReturn(new BigDecimal("100"));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.rentalToDto(any(Rental.class))).thenReturn(rentalDto);

        // Act
        rentalService.updateRental(1L, rentalDto);

        // Assert
        // Verifica que el precio se haya recalculado.
        verify(pricingService, times(1)).calculateDailyRate(vehicle.getVehicleType(), rental.getChosenPricingTier());
        verify(rentalRepository, times(1)).save(rental);
    }

    // Caso borde: Actualizar un alquiler con fechas inválidas.
    @Test
    void updateRental_shouldThrowBadRequestException_whenStartDateIsAfterEndDate() {
        // Arrange
        rentalDto.setStartDate(LocalDateTime.now().plusDays(3));
        rentalDto.setEndDate(LocalDateTime.now().plusDays(1));
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            rentalService.updateRental(1L, rentalDto);
        });
        assertEquals("La fecha de inicio no puede ser posterior a la fecha de fin.", exception.getMessage());
    }

    // Caso de éxito: Encontrar un alquiler por su ID.
    @Test
    void findRentalById_shouldReturnRental_whenExists() {
        // Arrange
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalMapper.rentalToDto(any(Rental.class))).thenReturn(rentalDto);

        // Act
        RentalDto found = rentalService.findRentalById(1L);

        // Assert
        assertNotNull(found);
        assertEquals(rentalDto, found);
    }

    // Caso borde: Intentar encontrar un alquiler que no existe.
    @Test
    void findRentalById_shouldThrowNotFoundException_whenNotExists() {
        // Arrange
        when(rentalRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            rentalService.findRentalById(1L);
        });
    }

    // Caso de éxito: Eliminar un alquiler.
    @Test
    void deleteRental_shouldSucceed_andMakeVehicleAvailable() {
        // Arrange
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        // Act
        rentalService.deleteRental(1L);

        // Assert
        // Verifica que el estado del vehículo se restauró a AVAILABLE.
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        verify(vehicleRepository, times(1)).save(vehicle);
        // Verifica que el método delete del repositorio fue llamado.
        verify(rentalRepository, times(1)).delete(rental);
    }
}