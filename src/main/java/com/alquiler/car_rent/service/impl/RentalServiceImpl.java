package com.alquiler.car_rent.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.RentalService;

@Service
public class RentalServiceImpl implements RentalService{
	private final RentalRepository rentalRepository;
	private final VehicleRepository vehicleRepository;
	
	public RentalServiceImpl(RentalRepository rentalRepository, VehicleRepository vehicleRepository) {
		this.rentalRepository = rentalRepository;
		this.vehicleRepository = vehicleRepository;
	}

	@Override
	public List<Rental> findAllRentals() {
		
		return rentalRepository.findAll();
	}

	@Override
	public Optional<Rental> findRentalById(Long id) {
		
		return rentalRepository.findById(id);
	}

	@Override
	public Rental createRental(Rental rental) {
	      Vehicle vehicle = vehicleRepository.findById(rental.getVehicle().getId())
	    		  .orElseThrow(()-> new NotFoundException("Vehiculo No Encontrado por ID: " + rental.getVehicle().getId()));
	      if(vehicle.getStatus() != VehicleStatus.AVAILABLE) {
	    	  throw new IllegalArgumentException("El vehiculo no esta Disponible para alquiler");
	      }
	      
	      //Actualiza el estado del vehiculo
	      vehicle.setStatus(VehicleStatus.RENTED);
	      vehicleRepository.save(vehicle);
	      
	      //Info de Alquiler
	      rental.setCreatedAt(LocalDateTime.now());
	      rental.setRentalStatus(RentalStatus.ACTIVE);
	      
		return rentalRepository.save(rental);
	}

	@Override
	public Rental updateRental(Long id, Rental rental) {
		return rentalRepository.findById(id)
                .map(existingRental -> {
                    existingRental.setStartDate(rental.getStartDate());
                    existingRental.setEndDate(rental.getEndDate());
                    existingRental.setTotalPrice(rental.getTotalPrice());
                    return rentalRepository.save(existingRental);
                })
                .orElseThrow(() -> new NotFoundException("Alquiler no encontrado con ID: " + id));
	}
	
	public Rental cancelRental(Long id) {
	    return rentalRepository.findById(id)
	            .map(rental -> {
	                rental.setRentalStatus(RentalStatus.CANCELLED);
	                return rentalRepository.save(rental);
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
