package com.alquiler.car_rent.repositories;

import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.enums.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    // Consultas básicas
    List<Rental> findByRentalStatus(RentalStatus status);

    // Consulta única optimizada con paginación
    @Query("""
    SELECT r FROM Rental r
    WHERE
        (:start IS NULL OR r.endDate >= :start)
        AND (:end IS NULL OR r.startDate <= :end)
""")

    Page<Rental> searchByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // Conteo directo sin cargar registros
    @Query("""
        SELECT COUNT(r) FROM Rental r
        WHERE
            (:start IS NULL OR r.startDate <= :end) AND
            (:end IS NULL OR r.endDate >= :start)
    """)
    long countByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Consulta nativa optimizada para tendencias
    @Query("""
    SELECT 
        FUNCTION('DATE_FORMAT', r.startDate, '%Y-%m') AS period,
        COUNT(r) AS rentalCount 
    FROM Rental r 
    WHERE r.startDate <= :end AND r.endDate >= :start
    GROUP BY period 
    ORDER BY period
""")
    List<Map<String, Object>> findRentalTrends(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Proyección para métricas de ingresos
    @Query("""
        SELECT COALESCE(SUM(r.totalPrice), 0.0)
        FROM Rental r
        WHERE r.startDate <= :end AND r.endDate >= :start
    """)
    double getTotalRevenueInRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Para promedio de duración por cliente
    @Query("""
    SELECT
        c.name AS customerName,
        AVG(DATEDIFF(r.endDate, r.startDate)) AS avgDays
    FROM Rental r
    JOIN r.customer c
    WHERE r.startDate <= :end AND r.endDate >= :start
        AND c.id IN :customerIds
    GROUP BY c.name
""")
    List<Object[]> findAverageDurationByCustomer(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("customerIds") List<Long> customerIds
    );

    // Para uso de vehículos
    @Query("""
    SELECT v.id AS vehicleId, v.brand AS brand, v.model AS model, COUNT(r) AS usageCount 
    FROM Rental r 
    JOIN r.vehicle v 
    WHERE r.startDate <= :end AND r.endDate >= :start
    GROUP BY v.id, v.brand, v.model 
""")
    List<Map<String, Object>> findVehicleUsage(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Para encontrar el vehículo más alquilado
    @Query("""
    SELECT
    v.brand AS brand,
    v.model AS model,
    COUNT(r) AS rentalCount
    FROM Rental r
    JOIN r.vehicle v
    WHERE r.startDate <= :end AND r.endDate >= :start
    GROUP BY v.brand, v.model
    ORDER BY rentalCount DESC
    """)
    List<Map<String,Object>> findMostRentedVehicle(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Para encontrar los principales clientes por número de alquileres
    @Query("""
    SELECT c.id AS customerId, c.name AS name, COUNT(r) AS rentalCount 
    FROM Rental r 
    JOIN Customer c ON r.customer.id = c.id 
    WHERE r.startDate <= :end AND r.endDate >= :start
    GROUP BY c.id, c.name 
    ORDER BY rentalCount DESC
""")
    List<Object[]> findTopCustomersByRentals(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}