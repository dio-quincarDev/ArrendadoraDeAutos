package com.alquiler.car_rent.service;

import java.io.IOException;

public interface JasperReportService {
	byte[] generateMonthlyRentalReport() throws IOException;

}
