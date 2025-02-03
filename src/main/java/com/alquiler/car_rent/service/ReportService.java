package com.alquiler.car_rent.service;

import java.io.File;
import java.io.IOException;

public interface ReportService {
	File generatedMostRentedCarChart()throws IOException;

}
