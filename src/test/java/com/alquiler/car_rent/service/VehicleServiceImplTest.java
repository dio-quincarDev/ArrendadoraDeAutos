package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.PricingTier;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.enums.VehicleType;
import com.alquiler.car_rent.commons.mappers.VehicleMapper;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle vehicle;
    private VehicleDto vehicleDto;

    @BeforeEach
    void setup() {
        // Objeto base para no repetir en cada prueba
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setYear(2022);
        vehicle.setPlate("PA-1234");
        vehicle.setVehicleType(VehicleType.SEDAN);
        vehicle.setPricingTier(PricingTier.STANDARD);
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        vehicleDto = new VehicleDto();
        vehicleDto.setId(1L);
        vehicleDto.setBrand("Toyota");
        vehicleDto.setModel("Corolla");
        vehicleDto.setYear(2022);
        vehicleDto.setPlate("PA-1234");
        vehicleDto.setVehicleType(VehicleType.SEDAN);
        vehicleDto.setPricingTier(PricingTier.STANDARD);
        vehicleDto.setStatus(VehicleStatus.AVAILABLE);
    }

    //<editor-fold desc="CREATE VEHICLE TESTS">
    @Test
    void testCreateVehicle_Success() {
        // Arrange
        when(vehicleMapper.dtoToVehicle(any(VehicleDto.class))).thenReturn(vehicle);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(vehicleMapper.vehicleToDto(any(Vehicle.class))).thenReturn(vehicleDto);

        // Act
        VehicleDto result = vehicleService.createVehicle(new VehicleDto());

        // Assert
        assertNotNull(result);
        assertEquals(VehicleStatus.AVAILABLE, result.getStatus());
        verify(vehicleRepository, times(1)).save(vehicle);
    }
    //</editor-fold>

    //<editor-fold desc="FIND VEHICLE TESTS">
    @Test
    void testFindVehicleById_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.vehicleToDto(vehicle)).thenReturn(vehicleDto);

        // Act
        VehicleDto result = vehicleService.findVehicleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(vehicleRepository, times(1)).findById(1L);
    }

    @Test
    void testFindVehicleById_NotFound() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            vehicleService.findVehicleById(99L);
        });
    }

    @Test
    void testFindAllVehicles_Success() {
        // Arrange
        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
        when(vehicleMapper.vehicleToDto(any(Vehicle.class))).thenReturn(vehicleDto);

        // Act
        List<VehicleDto> results = vehicleService.findAllVehicles();

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    void testFindAllVehicles_EmptyList() {
        // Arrange
        when(vehicleRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<VehicleDto> results = vehicleService.findAllVehicles();

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(vehicleRepository, times(1)).findAll();
    }

    @Test
    void testFindVehicleByStatus_Success() {
        // Arrange
        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE)).thenReturn(List.of(vehicle));
        when(vehicleMapper.vehicleToDto(any(Vehicle.class))).thenReturn(vehicleDto);

        // Act
        List<VehicleDto> results = vehicleService.findVehicleByStatus(VehicleStatus.AVAILABLE);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(VehicleStatus.AVAILABLE, results.get(0).getStatus());
        verify(vehicleRepository, times(1)).findByStatus(VehicleStatus.AVAILABLE);
    }
    //</editor-fold>

    //<editor-fold desc="UPDATE VEHICLE TESTS">
    @Test
    void testUpdateVehicle_Success() {
        // Arrange
        VehicleDto updateDto = new VehicleDto();
        updateDto.setModel("Corolla X");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.vehicleToDto(any(Vehicle.class))).thenReturn(vehicleDto);

        // Act
        vehicleService.updateVehicle(1L, updateDto);

        // Assert
        verify(vehicleRepository, times(1)).findById(1L);
        verify(vehicleMapper, times(1)).updateVehicleFromDto(updateDto, vehicle);
    }

    @Test
    void testUpdateVehicle_NotFound() {
        // Arrange
        VehicleDto updateDto = new VehicleDto();
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            vehicleService.updateVehicle(99L, updateDto);
        });
    }
    //</editor-fold>

    //<editor-fold desc="DELETE VEHICLE TESTS">
    @Test
    void testDeleteVehicle_Success() {
        // Arrange
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1L);

        // Act
        vehicleService.deleteVehicle(1L);

        // Assert
        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteVehicle_NotFound() {
        // Arrange
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            vehicleService.deleteVehicle(99L);
        });
        verify(vehicleRepository, never()).deleteById(anyLong());
    }
    //</editor-fold>
}