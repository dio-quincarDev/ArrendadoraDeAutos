package com.alquiler.car_rent.service.impl.reportsImpl;

import com.alquiler.car_rent.commons.constants.ReportingConstants;
import com.alquiler.car_rent.commons.entities.Vehicle;
import com.alquiler.car_rent.service.reportService.ChartReportService;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.category.*;
import org.jfree.data.xy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.batik.dom.GenericDOMImplementation;


import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@Service
public class ChartReportServiceImpl implements ChartReportService {

    private static final Logger logger = LoggerFactory.getLogger(ChartReportServiceImpl.class);
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    @Override
    public byte[] generateReport(Map<String, Object> data, ReportingConstants.ReportType reportType,
                                 ReportingConstants.OutputFormat format) {
        switch (format) {
            case CHART_PNG:
                return generateChartAsPng(data, reportType);
            case CHART_SVG:
                return generateChartAsSvg(data, reportType);
            default:
                throw new UnsupportedOperationException("Formato no soportado para gráficos: " + format);
        }
    }

    @Override
    public byte[] generateChartAsPng(Map<String, Object> data, ReportingConstants.ReportType reportType) {
        JFreeChart chart = createChart(data, reportType);
        if (chart == null) return generatePlaceholderPng(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BufferedImage image = chart.createBufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error generating PNG chart: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PNG chart", e);
        }
    }

    @Override
    public byte[] generateChartAsSvg(Map<String, Object> data, ReportingConstants.ReportType reportType) {
        JFreeChart chart = createChart(data, reportType);
        if (chart == null) return new byte[0];
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGraphics2D svgGraphics2D = new SVGGraphics2D(document);
        chart.draw(svgGraphics2D, new Rectangle(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        Element svgElement = svgGraphics2D.getRoot();
        svgElement.setAttribute("viewBox", "0 0 " + DEFAULT_WIDTH + " " + DEFAULT_HEIGHT);
        svgElement.setAttribute("width", "100%");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            svgGraphics2D.stream(writer, true);
            writer.flush();
            svgGraphics2D.dispose();
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error generating SVG chart: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating SVG chart", e);
        }
    }

    private byte[] generatePlaceholderPng(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Error generating placeholder PNG: {}", e.getMessage(), e);
            return new byte[0];
        }
    }

    @Override
    public String getReportTitle(ReportingConstants.ReportType reportType) {
        return reportType.getTitle();
    }

    private JFreeChart createChart (Map < String, Object > data, ReportingConstants.ReportType reportType){
            switch (reportType) {
                case MOST_RENTED_VEHICLES:
                    DefaultCategoryDataset horizontalBarDataset = new DefaultCategoryDataset();
                    Map<Vehicle, Long> rentalCounts = (Map<Vehicle, Long>) data.get("rentalCountsByVehicle");
                    if (rentalCounts != null) {
                        rentalCounts.entrySet().stream()
                                .sorted(Map.Entry.<Vehicle, Long>comparingByValue().reversed())
                                .limit(10)
                                .forEach(entry -> {
                                    String vehicle = entry.getKey().getBrand() + " " + entry.getKey().getModel();
                                    horizontalBarDataset.addValue(entry.getValue(), "Alquileres", vehicle);
                                });
                        JFreeChart chart = ChartFactory.createBarChart(
                                "Vehículos más alquilados", "Vehículo", "Cantidad de alquileres", horizontalBarDataset);
                        CategoryPlot plot = chart.getCategoryPlot();
                        ((BarRenderer) plot.getRenderer()).setDefaultItemLabelsVisible(true);
                        plot.setOrientation(PlotOrientation.HORIZONTAL);
                        return chart;
                    }
                    return null;

                case RENTAL_TRENDS:
                    DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();
                    List<Map<String, Object>> rentalTrends = (List<Map<String, Object>>) data.get("rentalTrends");
                    if (rentalTrends != null) {
                        for (Map<String, Object> trend : rentalTrends) {
                            String period = (String) trend.get("period");
                            Number rentalCount = (Number) trend.get("rentalCount");
                            Number totalRevenue = (Number) trend.get("totalRevenue");
                            
                            lineDataset.addValue(rentalCount, "Alquileres", period);
                            lineDataset.addValue(totalRevenue, "Ingresos", period);
                        }
                        
                        JFreeChart chart = ChartFactory.createLineChart(
                            "Tendencias de Alquileres", 
                            "Período", 
                            "Cantidad", 
                            lineDataset
                        );
                        
                        // CORRECCIÓN: Obtener el plot del chart
                        CategoryPlot plot = chart.getCategoryPlot();
                        
                        // Configurar eje secundario para ingresos
                        NumberAxis revenueAxis = new NumberAxis("Ingresos ($)");
                        revenueAxis.setAutoRangeIncludesZero(false);
                        plot.setRangeAxis(1, revenueAxis);
                        
                        // Mapear la segunda serie al eje secundario
                        plot.mapDatasetToRangeAxis(1, 1);
                        
                        // Personalizar colores
                        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
                        renderer.setSeriesPaint(0, Color.BLUE);   // Alquileres
                        renderer.setSeriesPaint(1, Color.GREEN);  // Ingresos
                        
                        return chart;
                    }
                    return null;

                case VEHICLE_USAGE:
                    DefaultCategoryDataset usageDataset = new DefaultCategoryDataset();
                    List<Map<String, Object>> vehicleUsage = (List<Map<String, Object>>) data.get("vehicleUsage");
                    if (vehicleUsage != null) {
                        for(Map<String,Object> entry : vehicleUsage){
                            String label = (String) entry.get("brandModel");
                            Number count = ((Number) entry.get("usageCount"));
                            usageDataset.addValue(count, "Uso", label);
                        }
                        JFreeChart chart = ChartFactory.createBarChart(
                                "Uso de Vehículos", "Vehículos", "Cantidad de Usos", usageDataset);
                        CategoryPlot plot = chart.getCategoryPlot();
                        ((BarRenderer) plot.getRenderer()).setDefaultItemLabelsVisible(true);
                        plot.setOrientation(PlotOrientation.HORIZONTAL);
                        return chart;
                    }
                    return null;

                case CUSTOMER_ACTIVITY:
                    XYSeriesCollection scatterDataset = new XYSeriesCollection();
                    List<Map<String, Number>> customerActivity = (List<Map<String, Number>>) data.get("customerActivity");
                    if (customerActivity != null) {
                        XYSeries series = new XYSeries("Clientes");
                        customerActivity.forEach(activity -> {
                            Number x = activity.get("rentals");
                            Number y = activity.get("revenue");
                            if (x != null && y != null) {
                                series.add(x.doubleValue(), y.doubleValue());
                            }
                        });
                        scatterDataset.addSeries(series);
                        JFreeChart chart = ChartFactory.createScatterPlot(
                                "Actividad de Clientes", "Número de Alquileres", "Ingresos Generados", scatterDataset);
                        XYPlot plot = chart.getXYPlot();
                        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
                        renderer.setSeriesShape(0, new Ellipse2D.Double(-5, -5, 10, 10));
                        renderer.setDefaultLinesVisible(false);
                        return chart;
                    }
                    return null;

                case AVERAGE_RENTAL_DURATION:
                    DefaultCategoryDataset durationDataset = new DefaultCategoryDataset();
                    Map<String, Double> avgDurationByCustomer = (Map<String, Double>) data.get("averageRentalDurationByTopCustomers");
                    if (avgDurationByCustomer != null) {
                        avgDurationByCustomer.forEach((customer, days) -> durationDataset.addValue(days, "Días Promedio", customer));
                        JFreeChart chart = ChartFactory.createBarChart(
                                "Duración Promedio de Alquiler por Cliente", "Cliente", "Días Promedio", durationDataset);
                        CategoryPlot plot = chart.getCategoryPlot();
                        ((BarRenderer) plot.getRenderer()).setDefaultItemLabelsVisible(true);
                        plot.setOrientation(PlotOrientation.HORIZONTAL);
                        return chart;
                    }
                    return null;

                case TOP_CUSTOMERS_BY_RENTALS:
                    DefaultCategoryDataset customerDataset = new DefaultCategoryDataset();
                    List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) data.get("topCustomersByRentals");
                    if (topCustomers != null) {
                        topCustomers.forEach(entry -> {
                            String customer = (String) entry.get("name");
                            Number rentalCount = (Number) entry.get("rentalCount");
                            customerDataset.addValue(rentalCount, "Alquileres", customer);
                        });
                        JFreeChart chart = ChartFactory.createBarChart(
                                "Clientes más frecuentes en alquileres", "Cliente", "Cantidad de Alquileres", customerDataset);
                        CategoryPlot plot = chart.getCategoryPlot();
                        ((BarRenderer) plot.getRenderer()).setDefaultItemLabelsVisible(true);
                        return chart;
                    }
                    return null;

                case RENTAL_SUMMARY:
                    DefaultCategoryDataset rentalSummaryDataset = new DefaultCategoryDataset();
                    Long totalRentals = (Long) data.get("totalRentals");
                    String periodLabel = "Período del Resumen"; // Podemos hacer esto más dinámico si el período está en los datos
                    if (totalRentals != null) {
                        rentalSummaryDataset.addValue(totalRentals, "Total Alquileres", periodLabel);
                        JFreeChart chart = ChartFactory.createBarChart(
                                "Resumen de Alquileres", "Período", "Total de Alquileres", rentalSummaryDataset);
                        CategoryPlot plot = chart.getCategoryPlot();
                        ((BarRenderer) plot.getRenderer()).setDefaultItemLabelsVisible(true);
                        return chart;
                    }
                    return null;

                case REVENUE_ANALYSIS:
                    DefaultCategoryDataset stackedAreaDataset = new DefaultCategoryDataset();
                    Map<String, Map<String, Double>> revenueComposition = (Map<String, Map<String, Double>>) data.get("revenueComposition");
                    if (revenueComposition != null) {
                        revenueComposition.forEach((category, monthlyData) -> {
                            monthlyData.forEach((month, revenue) -> stackedAreaDataset.addValue(revenue, category, month));
                        });
                        return ChartFactory.createStackedAreaChart(
                                "Composición de Ingresos", "Mes", "Ingresos", stackedAreaDataset);
                    }
                    return null;

                default:
                    logger.warn("Tipo de gráfico no implementado: {}", reportType);
                    return null;
            }
        }
    }