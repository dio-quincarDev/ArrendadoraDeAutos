package com.alquiler.car_rent.commons.mappers;

import org.mapstruct.Mapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper
public interface DateMapper {
    default String map(LocalDateTime date) {
        return date != null 
            ? date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            : null;
    }
    
    default LocalDateTime map(String date) {
        return date != null 
            ? LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            : null;
    }
}