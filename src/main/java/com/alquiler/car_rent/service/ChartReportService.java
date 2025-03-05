package com.alquiler.car_rent.service;

import java.io.IOException;

public interface ChartReportService {
	byte[] generateMostRentedCarChart() throws IOException;

}
