package com.alquiler.car_rent.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
						.description("Comprehensive vehicle rental management system API. " +
								"Provides endpoints for:\n" +
								"- Fleet management (vehicles, availability, maintenance)\n" +
								"- Customer relationship management\n" +
								"- Rental reservations and transactions\n" +
								"- Financial reporting and analytics\n" +
								"- SMS notification services")
						.contact(new Contact()
								.name("Diogenes Quintero")
								.email("dio-quincar@outlook.com")
								.url("https://www.linkedin.com/in/dio-quincar"))
						.license(new License()
								.name("Apache 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0.html")))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName,
								new SecurityScheme()
										.name(HttpHeaders.AUTHORIZATION)
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.in(SecurityScheme.In.HEADER)
										.description("JWT authentication required for protected endpoints. " +
												"Obtain token through authentication endpoints.\n" +
												"Format: `Bearer <your-token>`")));
	}
}