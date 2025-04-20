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

    // Consulta optimizada para búsqueda entre fechas
    @Query("SELECT r FROM Rental r WHERE " +
            "(:start IS NULL OR r.endDate >= :start) AND " +
            "(:end IS NULL OR r.startDate <= :end)")
    Page<Rental> search(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        Pageable pageable);

    // Consulta para obtener alquileres en un rango de fechas (no paginada)
    @Query("SELECT r FROM Rental r WHERE " +
            "(:start IS NULL OR r.endDate >= :start) AND " +
            "(:end IS NULL OR r.startDate <= :end)")
    List<Rental> findInDateRange(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    // Método para contar alquileres en un rango de fechas (más eficiente)
    @Query("SELECT COUNT(r) FROM Rental r WHERE " +
            "(:start IS NULL OR r.endDate >= :start) AND " +
            "(:end IS NULL OR r.startDate <= :end)")
    long countInDateRange(@Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
}