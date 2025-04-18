package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.*;
import com.alquiler.car_rent.commons.dtos.RentalDto;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Customer;
import com.alquiler.car_rent.commons.entities.Vehicle;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {DateMapper.class}
)
public interface RentalMapper {

    // Mapeo Entity -> DTO
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleBrand", source = "vehicle.brand")
    @Mapping(target = "vehicleModel", source = "vehicle.model")
    @Mapping(target = "totalPrice", qualifiedByName = "scaleBigDecimal")
    RentalDto rentalToDto(Rental rental);

    // Mapeo DTO -> Entity (para creación)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalPrice", qualifiedByName = "scaleBigDecimal")
    @Mapping(target = "createdAt", ignore = true)
    Rental dtoToRental(RentalDto rentalDto);

    // Mapeo para actualizaciones parciales
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "totalPrice", qualifiedByName = "scaleBigDecimal")
    void updateRentalFromDto(RentalDto rentalDto, @MappingTarget Rental rental);

    // Custom BigDecimal handler
    @Named("scaleBigDecimal")
    default BigDecimal scaleBigDecimal(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP) : null;
    }

    // Mapeo de relaciones después del mapeo principal
    @AfterMapping
    default void mapRelations(@MappingTarget Rental rental, RentalDto dto) {
        if (dto.getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setId(dto.getCustomerId());
            rental.setCustomer(customer);
        }
        
        if (dto.getVehicleId() != null) {
            Vehicle vehicle = new Vehicle();
            vehicle.setId(dto.getVehicleId());
            rental.setVehicle(vehicle);
        }
    }
}