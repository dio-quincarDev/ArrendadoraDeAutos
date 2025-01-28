package com.alquiler.car_rent.config;



import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com/alquiler/car_rent/controllers",
        "com/alquiler/car_rent/repositories",
        "com/alquiler/car_rent/commons",
        "com/alquiler/car_rent/config"
})
public class ComponentScanConfig {
	
}

