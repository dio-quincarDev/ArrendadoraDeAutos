package com.alquiler.car_rent.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.mappers.RentalMapper;
import com.alquiler.car_rent.exceptions.InvalidRentalException;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.RentalService;

@Service
public class RentalServiceImpl implements RentalService{
	private final RentalRepository rentalRepository;
	private final VehicleRepository vehicleRepository;
	private final CustomerRepository customerRepository;
	private final RentalMapper rentalMapper;
	
	public RentalServiceImpl(RentalRepository rentalRepository, VehicleRepository vehicleRepository, 
			CustomerRepository customerRepository, RentalMapper rentalMapper) {
		this.rentalRepository = rentalRepository;
		this.vehicleRepository = vehicleRepository;
		this.customerRepository = customerRepository;
		this.rentalMapper = rentalMapper;
	}
    
	@Override
	@Transactional(readOnly = true)
	public List<RentalDto> findAllRentals() {
		
		return rentalRepository.findAll()
				.stream()
				.map(rentalMapper::rentalToDto)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<RentalDto> findRentalById(Long id) {
		
		return rentalRepository.findById(id).map(rentalMapper::rentalToDto);
	}

	@Override
	public RentalDto createRental(RentalDto rentalDto) {
		
		if (rentalDto.getStartDate().toString().length() > 25 ||
			    rentalDto.getEndDate().toString().length() > 25) {
			  throw new IllegalArgumentException("Formato de fecha de alquiler no válido");
			}

		
		Rental rental = rentalMapper.dtoToRental(rentalDto);
		
		
		 // Obtener el Cliente
	    rental.setCustomer(customerRepository.findById(rentalDto.getCustomerId())
	        .orElseThrow(() -> new NotFoundException("Cliente No Encontrado por ID: " + rentalDto.getCustomerId())));

	    // Obtener el Vehículo
	    Vehicle vehicle = vehicleRepository.findById(rentalDto.getVehicleId())
	        .orElseThrow(() -> new NotFoundException("Vehiculo No Encontrado por ID: " + rentalDto.getVehicleId()));

	    // Validar disponibilidad
	    if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
	        throw new IllegalArgumentException("El vehiculo no está disponible para alquiler");
	    }
	      //Actualiza el estado del vehiculo
	      vehicle.setStatus(VehicleStatus.RENTED);
	      vehicleRepository.save(vehicle);
	      
	      //Info de Alquiler
	      rental.setVehicle(vehicle);
	      rental.setCreatedAt(LocalDateTime.now());
	      rental.setRentalStatus(RentalStatus.ACTIVE);
	      
		return rentalMapper.rentalToDto(rentalRepository.save(rental));
	}

	@Override
	public RentalDto updateRental(Long id, RentalDto rentalDto) {
		
		return rentalRepository.findById(id)
                .map(existingRental -> {
                	if (rentalDto.getStartDate().toString().length() > 25 ||
                		    rentalDto.getEndDate().toString().length() > 25) {
                		  throw new IllegalArgumentException("Formato de fecha de alquiler no válido");
                		}

                    existingRental.setStartDate(rentalDto.getStartDate());
                    existingRental.setEndDate(rentalDto.getEndDate());
                    existingRental.setTotalPrice(rentalDto.getTotalPrice());
                    return rentalMapper.rentalToDto(rentalRepository.save(existingRental));
                })
                .orElseThrow(() -> new NotFoundException("Alquiler no encontrado con ID: " + id));
	}
	
	
	@Override
	public RentalDto cancelRental(Long id) {
	    return rentalRepository.findById(id)
	            .map(rental -> {
	            	 Vehicle vehicle = rental.getVehicle();
	                 vehicle.setStatus(VehicleStatus.AVAILABLE);
	                 vehicleRepository.save(vehicle);
	            	
	                rental.setRentalStatus(RentalStatus.CANCELLED);
	                rentalRepository.save(rental);
	                return rentalMapper.rentalToDto(rentalRepository.save(rental));
	            })
	            .orElseThrow(() -> new NotFoundException("Alquiler no encontrado con ID: " + id));
	}


	@Override
	public void deleteRental(Long id) {
	     if (!rentalRepository.existsById(id)) {
	            throw new NotFoundException("Alquiler no encontrado con ID: " + id);
	        }
	        rentalRepository.deleteById(id);
	    }

}
