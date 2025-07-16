package com.alquiler.car_rent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.flyway.baseline-on-migrate=true")
class CarRentApplicationTests {

	@Test
	void contextLoads() {
	}

}
