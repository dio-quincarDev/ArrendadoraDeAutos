package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import com.alquiler.car_rent.commons.enums.VehicleStatus;
import com.alquiler.car_rent.commons.mappers.RentalMapper;
import com.alquiler.car_rent.exceptions.BadRequestException;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.repositories.VehicleRepository;
import com.alquiler.car_rent.service.PricingService;
import com.alquiler.car_rent.service.RentalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RentalServiceImpl implements RentalService{
	private final RentalRepository rentalRepository;
	private final VehicleRepository vehicleRepository;
	private final CustomerRepository customerRepository;
	private final RentalMapper rentalMapper;
	private final PricingService pricingService;
	
	public RentalServiceImpl(RentalRepository rentalRepository, VehicleRepository vehicleRepository, 
			CustomerRepository customerRepository, RentalMapper rentalMapper, PricingService pricingService) {
		this.rentalRepository = rentalRepository;
		this.vehicleRepository = vehicleRepository;
		this.customerRepository = customerRepository;
		this.rentalMapper = rentalMapper;
		this.pricingService = pricingService;
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
	public RentalDto findRentalById(Long id) {
		return rentalRepository.findById(id)
                .map(rentalMapper::rentalToDto)
                .orElseThrow(() -> new NotFoundException("Alquiler no encontrado con ID: " + id));
	}

	@Override
	public RentalDto createRental(RentalDto rentalDto) {
		
		if (rentalDto.getStartDate().isAfter(rentalDto.getEndDate())) {
			throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin.");
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
	        throw new BadRequestException("El vehiculo no está disponible para alquiler");
	    }

		// --- INICIO DE LA LÓGICA DE CÁLCULO DE PRECIOS CENTRALIZADA ---
		// Validar que el chosenPricingTier no sea nulo
		if (rentalDto.getChosenPricingTier() == null) {
			throw new BadRequestException("El nivel de precios elegido (chosenPricingTier) es requerido.");
		}

		BigDecimal dailyRate = pricingService.calculateDailyRate(vehicle.getVehicleType(), rentalDto.getChosenPricingTier());
		long rentalDays = ChronoUnit.DAYS.between(rentalDto.getStartDate(), rentalDto.getEndDate());
		if (rentalDays <= 0) {
			rentalDays = 1; // Mínimo se cobra un día
		}
		BigDecimal totalPrice = dailyRate.multiply(new BigDecimal(rentalDays));
		rental.setTotalPrice(totalPrice);
		rental.setChosenPricingTier(rentalDto.getChosenPricingTier());
		// --- FIN DE LA LÓGICA DE CÁLCULO DE PRECIOS CENTRALIZADA ---


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
                	if (rentalDto.getStartDate().isAfter(rentalDto.getEndDate())) {
                		  throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin.");
                		}

                    boolean datesChanged = !existingRental.getStartDate().equals(rentalDto.getStartDate()) ||
                                           !existingRental.getEndDate().equals(rentalDto.getEndDate());
                    boolean pricingTierChanged = rentalDto.getChosenPricingTier() != null &&
                                                 !rentalDto.getChosenPricingTier().equals(existingRental.getChosenPricingTier());

                    existingRental.setStartDate(rentalDto.getStartDate());
                    existingRental.setEndDate(rentalDto.getEndDate());

                    if (rentalDto.getChosenPricingTier() != null) {
                        existingRental.setChosenPricingTier(rentalDto.getChosenPricingTier());
                    }

                    if (datesChanged || pricingTierChanged) {
                        Vehicle vehicle = existingRental.getVehicle();
                        if (vehicle == null) {
                            throw new IllegalStateException("Vehículo asociado al alquiler no encontrado.");
                        }
                        // Usar el chosenPricingTier de la entidad, que ya fue actualizado si se proporcionó en el DTO
                        BigDecimal dailyRate = pricingService.calculateDailyRate(vehicle.getVehicleType(), existingRental.getChosenPricingTier());
                        long rentalDays = ChronoUnit.DAYS.between(existingRental.getStartDate(), existingRental.getEndDate());
                        if (rentalDays <= 0) {
                            rentalDays = 1;
                        }
                        BigDecimal newTotalPrice = dailyRate.multiply(new BigDecimal(rentalDays));
                        existingRental.setTotalPrice(newTotalPrice);
                    }

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
