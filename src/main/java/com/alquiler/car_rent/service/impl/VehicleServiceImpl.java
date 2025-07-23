package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.mappers.VehicleMapper;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {
	private final VehicleRepository vehicleRepository;
	private final VehicleMapper vehicleMapper;
	
	public VehicleServiceImpl (VehicleRepository vehicleRepository, VehicleMapper vehicleMapper) {
		this.vehicleRepository = vehicleRepository;
		this.vehicleMapper = vehicleMapper;
	}

	@Override
    @Transactional(readOnly = true)
	public List<VehicleDto> findAllVehicles() {
		return vehicleRepository.findAll()
				.stream()
				.map(vehicleMapper::vehicleToDto)
				.toList();
	}

	@Override
    @Transactional(readOnly = true)
	public VehicleDto findVehicleById(Long id) {
		return vehicleRepository.findById(id)
                .map(vehicleMapper::vehicleToDto)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado con ID: " + id));
	}

	@Override
    @Transactional(readOnly = true)
	public List<VehicleDto> findVehicleByStatus(VehicleStatus status) {
		   return vehicleRepository.findByStatus(status)
				.stream()
				.map(vehicleMapper::vehicleToDto)
				.toList();
	}

	@Override
    @Transactional
	public VehicleDto createVehicle(VehicleDto vehicleDto) {
		Vehicle vehicle = vehicleMapper.dtoToVehicle(vehicleDto);
		vehicle.setStatus(VehicleStatus.AVAILABLE);
		Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.vehicleToDto(savedVehicle);
	}

	@Override
    @Transactional
	public VehicleDto updateVehicle(Long id, VehicleDto vehicleDto) {
		return vehicleRepository.findById(id)
				.map(existingVehicle-> {
					vehicleMapper.updateVehicleFromDto(vehicleDto, existingVehicle);
					// No es necesario llamar a save() aquí, la transacción se encargará de persistir los cambios.
					return vehicleMapper.vehicleToDto(existingVehicle);
				})
		.orElseThrow(()-> new NotFoundException("Vehiculo no encontrado con el ID" + id));
	}

	@Override
    @Transactional
	public void deleteVehicle(Long id) {
		   if (!vehicleRepository.existsById(id)) {
	            throw new NotFoundException("Vehículo no encontrado con ID: " + id);
	        }
	        vehicleRepository.deleteById(id);
	}
	
}
