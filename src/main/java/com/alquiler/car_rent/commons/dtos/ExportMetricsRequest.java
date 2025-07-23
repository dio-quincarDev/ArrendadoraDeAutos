package com.alquiler.car_rent.commons.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Solicitud para exportar métricas en formato tabular")
public class ExportMetricsRequest {
    @Schema(
            description = "Lista de encabezados de columna para el reporte",
            example = "[\"Fecha\", \"Ingresos\"]"
    )
    private List<String> headers;

    @Schema(
            description = "Datos del reporte, donde cada lista interna representa una fila",
            example = "[[\"2023-01-01\", \"1500.00\"]]",
            implementation = List.class
    )
    private List<List<String>> data;

    @Schema(
            description = "Formato de exportación deseado (ej. PDF, EXCEL, CSV)",
            example = "EXCEL",
            allowableValues = {"PDF", "EXCEL", "CSV"}
    )
    private String format;
}