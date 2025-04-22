package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.service.reportService.JsonReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class JsonReportServiceImpl implements JsonReportService {
    private static final Logger logger = LoggerFactory.getLogger(JsonReportServiceImpl.class);
    private final ObjectMapper objectMapper;

    public JsonReportServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper =  objectMapper;
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    }

    @Override
    public byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType, ReportingConstants.OutputFormat format) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (IOException e) {
            logger.error("Error al convertir los datos en el reporte JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar reporte JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public String getReportTitle(ReportingConstants.ReportType reportType) {
        return reportType.getTitle() + "(JSON)";
    }
}
