package com.alquiler.car_rent.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "JWT";

		return new OpenAPI()
				.info(new Info()
						.title("Car Rental Management API")
						.version("1.0")
						.description("API para el sistema de gestión de alquiler de vehículos. " +
								"Incluye módulos para administración de vehículos, clientes, rentas, " +
								"generación de reportes y notificaciones SMS."))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName,
								new SecurityScheme()
										.name(HttpHeaders.AUTHORIZATION) // 🔑 Nombre exacto del header
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.in(SecurityScheme.In.HEADER)
										.description("Autenticación mediante token JWT. " +
												"Obtenga el token desde los endpoints de autenticación.")));
	}
}