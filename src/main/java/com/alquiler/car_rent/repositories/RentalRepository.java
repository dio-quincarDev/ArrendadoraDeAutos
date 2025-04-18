package com.alquiler.car_rent.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.enums.RentalStatus;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    
    // Consultas básicas
    List<Rental> findByRentalStatus(RentalStatus status);
    
    // Consultas personalizadas
    @Query("SELECT r FROM Rental r WHERE r.endDate >= :start AND r.endDate < :end")
    List<Rental> findInDateRange(@Param("start") LocalDateTime start, 
                                @Param("end") LocalDateTime end);
    
    @Query("SELECT r FROM Rental r WHERE " +
           "(:start IS NULL OR r.endDate >= :start) AND " +
           "(:end IS NULL OR r.endDate <= :end)")
    Page<Rental> search(@Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end,
                      Pageable pageable);
    
}