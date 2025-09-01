package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Rental;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.commons.enums.VehicleType;
import com.alquiler.car_rent.repositories.RentalRepository;
import com.alquiler.car_rent.service.impl.reportsImpl.ReportDataServiceImpl;
import com.alquiler.car_rent.service.reportService.ExcelReportService;
import com.alquiler.car_rent.service.reportService.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportDataServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private MetricsService metricsService;
    @Mock
    private ExcelReportService excelReportService;

    @InjectMocks
    private ReportDataServiceImpl reportDataService;

    private Rental testRental;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        // Inicializar pageSize manualmente para las pruebas unitarias
        reportDataService = new ReportDataServiceImpl(rentalRepository, metricsService, excelReportService);
        // Usar Reflection para establecer el campo pageSize, ya que es privado y no se inyecta con @Value en tests unitarios
        try {
            java.lang.reflect.Field pageSizeField = ReportDataServiceImpl.class.getDeclaredField("pageSize");
            pageSizeField.setAccessible(true);
            pageSizeField.set(reportDataService, 100); // Establecer un valor válido
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("No se pudo inicializar pageSize en ReportDataServiceImplTest: " + e.getMessage());
        }

        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setBrand("Toyota");
        testVehicle.setModel("Corolla");
        testVehicle.setVehicleType(VehicleType.SEDAN);

        // Usar fechas fijas para testRental para asegurar que caiga dentro de los rangos de prueba
        testRental = new Rental();
        testRental.setId(1L);
        testRental.setTotalPrice(new BigDecimal("100.00"));
        testRental.setStartDate(LocalDateTime.of(2025, 7, 15, 10, 0)); // Fecha fija dentro de un mes típico
        testRental.setEndDate(LocalDateTime.of(2025, 7, 20, 10, 0));   // Fecha fija
        testRental.setVehicle(testVehicle);
    }

    // Caso de éxito: Generar datos de reporte para un período específico (MONTHLY).
    @Test
    void generateReportData_shouldReturnCorrectData_forMonthlyPeriod() {
        // Arrange
        // Usar fechas fijas para el test para evitar flakiness con LocalDate.now()
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.of(2025, 7, 31);

        List<Rental> rentals = Collections.singletonList(testRental);

        when(rentalRepository.searchByDateRange(
                eq(startDate.atStartOfDay()), // Expected start date time
                eq(endDate.plusDays(1).atStartOfDay()), // Expected end date time (inclusive)
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(rentals));

        // Mockear las llamadas a metricsService
        when(metricsService.getUniqueCustomersRented(any(), any(), any())).thenReturn(5L);
        when(metricsService.getActiveCustomersCount(any(), any(), any())).thenReturn(3L);
        when(metricsService.getNewCustomersCount(any(), any(), any())).thenReturn(2L);
        when(metricsService.getCustomerActivity(any(), any(), any())).thenReturn(Collections.emptyList());
        when(metricsService.getTopCustomersByRentals(any(), any(), any(), eq(10))).thenReturn(Collections.emptyList());
        when(metricsService.getVehicleUsage(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getMostRentedVehicle(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getAvailableVehiclesCount()).thenReturn(10L);
        when(metricsService.getRentalTrends(any(), any(), any())).thenReturn(Collections.emptyList());
        when(metricsService.getAverageRentalDuration(any(), any(), any())).thenReturn(3.0);
        when(metricsService.getRentalsCountByVehicleType(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRevenueByVehicleType(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRentalsCountByPricingTier(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRevenueByPricingTier(any(), any(), any())).thenReturn(Collections.emptyMap());

        // Act
        Map<String, Object> reportData = reportDataService.generateReportData(
                ReportingConstants.TimePeriod.MONTHLY, startDate, endDate);

        // Assert
        assertNotNull(reportData);
        assertEquals(ReportingConstants.TimePeriod.MONTHLY.name(), reportData.get("period"));
        assertEquals(startDate, reportData.get("startDate"));
        assertEquals(endDate, reportData.get("endDate"));
        assertEquals(1, reportData.get("totalRentals"));
        assertEquals(100.0, (Double) reportData.get("totalRevenue"), 0.001);
        assertEquals(10L, reportData.get("availableVehicles"));
    }

    // Caso borde: Generar datos de reporte para ALL_TIME sin fechas específicas.
    @Test
    void generateReportData_shouldReturnAllTimeData_whenNoDatesProvided() {
        // Arrange
        List<Rental> rentals = Collections.singletonList(testRental);
        when(rentalRepository.searchByDateRange(
                eq(LocalDateTime.of(1900, 1, 1, 0, 0)), // SAFE_MIN_DATE
                eq(LocalDateTime.of(2150, 1, 1, 0, 0).plusDays(1)), // SAFE_MAX_DATE + 1 day
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(rentals));

        // Mockear las llamadas a metricsService (similar al test anterior)
        when(metricsService.getUniqueCustomersRented(any(), any(), any())).thenReturn(5L);
        when(metricsService.getActiveCustomersCount(any(), any(), any())).thenReturn(3L);
        when(metricsService.getNewCustomersCount(any(), any(), any())).thenReturn(2L);
        when(metricsService.getCustomerActivity(any(), any(), any())).thenReturn(Collections.emptyList());
        when(metricsService.getTopCustomersByRentals(any(), any(), any(), eq(10))).thenReturn(Collections.emptyList());
        when(metricsService.getVehicleUsage(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getMostRentedVehicle(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getAvailableVehiclesCount()).thenReturn(10L);
        when(metricsService.getRentalTrends(any(), any(), any())).thenReturn(Collections.emptyList());
        when(metricsService.getAverageRentalDuration(any(), any(), any())).thenReturn(3.0);
        when(metricsService.getRentalsCountByVehicleType(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRevenueByVehicleType(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRentalsCountByPricingTier(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRevenueByPricingTier(any(), any(), any())).thenReturn(Collections.emptyMap());

        // Act
        Map<String, Object> reportData = reportDataService.generateReportData(
                ReportingConstants.TimePeriod.ALL_TIME, null, null);

        // Assert
        assertNotNull(reportData);
        assertEquals(ReportingConstants.TimePeriod.ALL_TIME.name(), reportData.get("period"));
        // Las fechas de inicio y fin deberían ser las seguras por defecto
        assertEquals(LocalDate.of(1900, 1, 1), reportData.get("startDate"));
        assertEquals(LocalDate.of(2150, 1, 1), reportData.get("endDate"));
        assertEquals(1, reportData.get("totalRentals"));
        assertEquals(100.0, (Double) reportData.get("totalRevenue"), 0.001);
    }

    // Caso borde: Generar datos de reporte con una lista de alquileres vacía.
    @Test
    void generateReportData_shouldHandleEmptyRentalsList() {
        // Arrange
        List<Rental> rentals = Collections.emptyList();
        when(rentalRepository.searchByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(rentals));

        // Mockear las llamadas a metricsService para devolver valores por defecto/vacíos
        when(metricsService.getUniqueCustomersRented(any(), any(), any())).thenReturn(0L);
        when(metricsService.getActiveCustomersCount(any(), any(), any())).thenReturn(0L);
        when(metricsService.getNewCustomersCount(any(), any(), any())).thenReturn(0L);
        when(metricsService.getCustomerActivity(any(), any(), any())).thenReturn(Collections.emptyList());
        when(metricsService.getTopCustomersByRentals(any(), any(), any(), eq(10))).thenReturn(Collections.emptyList());
        when(metricsService.getVehicleUsage(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getMostRentedVehicle(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getAvailableVehiclesCount()).thenReturn(0L);
        when(metricsService.getRentalTrends(any(), any(), any())).thenReturn(Collections.emptyList());
        when(metricsService.getAverageRentalDuration(any(), any(), any())).thenReturn(0.0);
        when(metricsService.getRentalsCountByVehicleType(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRevenueByVehicleType(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRentalsCountByPricingTier(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(metricsService.getRevenueByPricingTier(any(), any(), any())).thenReturn(Collections.emptyMap());

        // Act
        Map<String, Object> reportData = reportDataService.generateReportData(
                ReportingConstants.TimePeriod.MONTHLY, LocalDate.now().minusMonths(1), LocalDate.now());

        // Assert
        assertNotNull(reportData);
        assertEquals(0, reportData.get("totalRentals"));
        assertEquals(0.0, (Double) reportData.get("totalRevenue"), 0.001);
        assertEquals(0L, reportData.get("availableVehicles"));
    }

    // Caso de éxito: Probar el método getRentalsInRange directamente.
    @Test
    void getRentalsInRange_shouldReturnRentals_whenCalledDirectly() {
        // Arrange
        List<Rental> rentals = Collections.singletonList(testRental);
        when(rentalRepository.searchByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(rentals));

        // Act
        List<Rental> result = reportDataService.getRentalsInRange(LocalDateTime.now().minusDays(10), LocalDateTime.now());

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRental, result.get(0));
    }

    // Caso de éxito: Probar el método toDateTime.
    @Test
    void toDateTime_shouldConvertLocalDateToLocalDateTime() {
        LocalDate date = LocalDate.of(2023, 1, 15);
        LocalDateTime expected = LocalDateTime.of(2023, 1, 15, 0, 0, 0);
        assertEquals(expected, reportDataService.toDateTime(date));
    }

    // Caso borde: Probar toDateTime con fecha nula.
    @Test
    void toDateTime_shouldReturnNull_whenDateIsNull() {
        assertNull(reportDataService.toDateTime(null));
    }

    // Caso de éxito: Probar el método formatDate.
    @Test
    void formatDate_shouldFormatLocalDateCorrectly() {
        LocalDate date = LocalDate.of(2023, 1, 15);
        assertEquals("15/01/2023", reportDataService.formatDate(date));
    }

    // Caso borde: Probar formatDate con fecha nula.
    @Test
    void formatDate_shouldReturnEmptyString_whenDateIsNull() {
        assertEquals("", reportDataService.formatDate(null));
    }

    // Caso de éxito: Probar generateGenericTableExcel (delegación).
    @Test
    void generateGenericTableExcel_shouldDelegateToExcelReportService() {
        List<String> headers = List.of("Header1");
        List<List<String>> data = List.of(List.of("Data1"));
        byte[] expectedBytes = "excel_bytes".getBytes();

        when(excelReportService.generateGenericTableExcel(headers, data)).thenReturn(expectedBytes);

        byte[] result = reportDataService.generateGenericTableExcel(headers, data);

        assertArrayEquals(expectedBytes, result);
    }
}
