package com.alquiler.car_rent.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.dtos.VehicleDto;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.mappers.VehicleMapper;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.VehicleService;

@Service
public class VehicleServiceImpl implements VehicleService {
	private final VehicleRepository vehicleRepository;
	private final VehicleMapper vehicleMapper;
	
	public VehicleServiceImpl (VehicleRepository vehicleRepository, VehicleMapper vehicleMapper) {
		this.vehicleRepository = vehicleRepository;
		this.vehicleMapper = vehicleMapper;
	}

	@Override
	public List<VehicleDto> findAllVehicles() {
		return vehicleRepository.findAll()
				.stream()
				.map(vehicleMapper::vehicleToDto)
				.toList();
	}

	@Override
	public Optional<VehicleDto> findVehicleById(Long id) {
		
		return vehicleRepository.findById(id).map(vehicleMapper::vehicleToDto);
	}

	@Override
	public List<VehicleDto> findVehicleByStatus(VehicleStatus status) {
		   return vehicleRepository.findByStatus(status)
				.stream()
				.map(vehicleMapper::vehicleToDto)
				.toList();
	}

	@Override
	public VehicleDto createVehicle(VehicleDto vehicleDto) {
		Vehicle vehicle = vehicleMapper.dtoToVehicle(vehicleDto);
		vehicle.setStatus(VehicleStatus.AVAILABLE);
		return vehicleMapper.vehicleToDto(vehicleRepository.save(vehicle));
	}

	@Override
	public VehicleDto updateVehicle(Long id, VehicleDto vehicleDto) {
		return vehicleRepository.findById(id)
				.map(existingVehicle-> {
					existingVehicle.setBrand(vehicleDto.brand());
					existingVehicle.setModel(vehicleDto.model());
					existingVehicle.setYear(vehicleDto.year());
					existingVehicle.setPlate(vehicleDto.plate());
					existingVehicle.setStatus(vehicleDto.status());
					vehicleRepository.save(existingVehicle);
					return vehicleMapper.vehicleToDto(vehicleRepository.save(existingVehicle));
				})
		.orElseThrow(()-> new IllegalArgumentException("Vehiculo no encontrado con el ID" + id));
	}

	@Override
	public void deleteVehicle(Long id) {
		   if (!vehicleRepository.existsById(id)) {
	            throw new IllegalArgumentException("Vehículo no encontrado con ID: " + id);
	        }
	        vehicleRepository.deleteById(id);
	}
	
}
