package com.alquiler.car_rent.config;

import com.alquiler.car_rent.commons.constants.ApiPathConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(ApiPathConstants.V1_ROUTE + "/**")
                .allowedOrigins("http://localhost:8080")  // Vue app's default port
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
