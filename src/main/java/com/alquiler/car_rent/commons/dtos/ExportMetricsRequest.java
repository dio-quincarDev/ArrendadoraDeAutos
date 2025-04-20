package com.alquiler.car_rent.commons.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ExportMetricsRequest {
    private List<String> headers;
    private List<List<String>> data;
    private String format;

}
