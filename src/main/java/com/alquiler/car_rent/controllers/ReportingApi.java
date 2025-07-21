package com.alquiler.car_rent.controllers;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.dtos.ExportMetricsRequest;
import com.alquiler.car_rent.exceptions.GlobalExceptionHandler.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(
        name = "Reports",
        description = "Generación de reportes y métricas para el dashboard administrativo"
)
@RequestMapping(ApiPathConstants.V1_ROUTE + ApiPathConstants.REPORTS_BASE_PATH)
public interface ReportingApi {

    @Operation(
            summary = "Obtener datos del dashboard",
            description = "Devuelve todas las métricas clave para el panel de control en formato JSON",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(
                            name = "period",
                            description = "Periodo de tiempo predefinido",
                            example = "MONTHLY",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = ReportingConstants.TimePeriod.class)
                    ),
                    @Parameter(
                            name = "startDate",
                            description = "Fecha de inicio personalizada (formato: yyyy-MM-dd)",
                            example = "2023-01-01",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "endDate",
                            description = "Fecha de fin personalizada (formato: yyyy-MM-dd)",
                            example = "2023-12-31",
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos del dashboard obtenidos exitosamente"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Parámetros de fecha inválidos",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"message\": \"La fecha de inicio no puede ser posterior a la fecha de fin\", \"status\": 400}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Acceso denegado (requiere rol ADMIN)"
                    )
            }
    )
    @GetMapping(produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(defaultValue = "MONTHLY") ReportingConstants.TimePeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @Operation(
            summary = "Exportar reporte",
            description = "Genera y descarga un reporte en el formato especificado (PDF, Excel, JSON, etc.)",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(
                            name = "format",
                            description = "Formato de salida del reporte",
                            example = "PDF",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = ReportingConstants.OutputFormat.class)
                    ),
                    @Parameter(
                            name = "reportType",
                            description = "Tipo de reporte a generar",
                            example = "RENTAL_SUMMARY",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = ReportingConstants.ReportType.class)
                    ),
                    @Parameter(
                            name = "period",
                            description = "Periodo de tiempo para el reporte",
                            example = "MONTHLY",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = ReportingConstants.TimePeriod.class)
                    ),
                    @Parameter(
                            name = "startDate",
                            description = "Fecha de inicio personalizada",
                            example = "2023-01-01",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "endDate",
                            description = "Fecha de fin personalizada",
                            example = "2023-12-31",
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte generado exitosamente",
                            content = @Content(
                                    mediaType = "application/octet-stream",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Parámetros inválidos",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error al generar el reporte",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<byte[]> exportReport(
            @RequestParam(defaultValue = "PDF") ReportingConstants.OutputFormat format,
            @RequestParam(defaultValue = "RENTAL_SUMMARY") ReportingConstants.ReportType reportType,
            @RequestParam(defaultValue = "MONTHLY") ReportingConstants.TimePeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @Operation(
            summary = "Exportar métricas genéricas",
            description = "Genera un archivo Excel con datos tabulares personalizados",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Archivo Excel generado exitosamente",
                            content = @Content(
                                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos de entrada inválidos",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/export-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<byte[]> exportMetrics(@RequestBody ExportMetricsRequest request);

    @Operation(
            summary = "Obtener total de alquileres",
            description = "Devuelve el número total de alquileres para el periodo especificado",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de tiempo", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Métrica obtenida exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/total-rentals")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Long> getTotalRentalsMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    );

    @Operation(
            summary = "Obtener ingresos totales",
            description = "Devuelve el total de ingresos generados por alquileres en el periodo",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de tiempo", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Métrica obtenida exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/total-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Double> getTotalRevenueMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    );

    @Operation(
            summary = "Obtener vehículos únicos alquilados",
            description = "Devuelve la cantidad de vehículos distintos que fueron alquilados",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de tiempo", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Métrica obtenida exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/unique-vehicles")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Long> getUniqueVehiclesRentedMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    );

    @Operation(
            summary = "Obtener vehículo más alquilado",
            description = "Identifica el vehículo con mayor número de alquileres en el periodo",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de tiempo", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos del vehículo más alquilado",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                            {
                              "brand": "Toyota",
                              "model": "Corolla",
                              "rentalCount": 15
                            }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/most-rented-vehicle")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Map<String, Object>> getMostRentedVehicleMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    );

    @Operation(
            summary = "Obtener conteo de nuevos clientes",
            description = "Devuelve el número de clientes nuevos registrados en el periodo",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de tiempo", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Métrica obtenida exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/new-customers")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Long> getNewCustomersCountMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    );

    @Operation(
            summary = "Obtener tendencias de alquiler",
            description = "Devuelve datos históricos de alquileres agrupados por periodo",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de agrupación", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos de tendencias obtenidos",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                            [
                              {
                                "period": "2023-01",
                                "rentalCount": 10,
                                "totalRevenue": 2500.00
                              },
                              {
                                "period": "2023-02",
                                "rentalCount": 15,
                                "totalRevenue": 3750.00
                              }
                            ]
                        """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/rental-trends")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<Map<String, Object>>> getRentalTrendsMetric(
            @RequestParam(value = "period", required = false) ReportingConstants.TimePeriod period,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @Operation(
            summary = "Obtener uso de vehículos",
            description = "Devuelve el recuento de alquileres por cada vehículo",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de tiempo", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos de uso obtenidos",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                            [
                              {
                                "brand": "Toyota",
                                "model": "Corolla",
                                "usageCount": 8
                              },
                              {
                                "brand": "Honda",
                                "model": "Civic",
                                "usageCount": 5
                              }
                            ]
                        """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/vehicle-usage")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<Map<String, Object>>> getVehicleUsageMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    );

    @Operation(
            summary = "Obtener duración promedio de alquiler",
            description = "Calcula la duración promedio de los alquileres por cliente",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de tiempo", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos de duración promedio",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                            [
                              {
                                "customer": "Juan Pérez",
                                "averageDuration": 5.2
                              },
                              {
                                "customer": "María Gómez",
                                "averageDuration": 3.8
                              }
                            ]
                        """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/average-rental-duration")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<Map<String, Object>>> getAverageRentalDurationMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    );

    @Operation(
            summary = "Obtener clientes principales",
            description = "Lista los clientes con más alquileres en el periodo",
            security = @SecurityRequirement(name = "JWT"),
            parameters = {
                    @Parameter(name = "period", description = "Periodo de tiempo", example = "MONTHLY"),
                    @Parameter(name = "startDate", description = "Fecha de inicio personalizada", example = "2023-01-01"),
                    @Parameter(name = "endDate", description = "Fecha de fin personalizada", example = "2023-12-31")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de clientes principales",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                            [
                              {
                                "name": "Juan Pérez",
                                "rentalCount": 12,
                                "totalSpent": 3000.00
                              },
                              {
                                "name": "María Gómez",
                                "rentalCount": 8,
                                "totalSpent": 2000.00
                              }
                            ]
                        """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/metrics/top-customers")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<Map<String, Object>>> getTopCustomersMetric(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "period", required = false, defaultValue = "MONTHLY") ReportingConstants.TimePeriod period
    );
}