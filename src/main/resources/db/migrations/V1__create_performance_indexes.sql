-- V4: Crea los indices para optimizar el rendimiento de las consultas.

-- Indices para la tabla 'vehicles'
CREATE INDEX idx_vehicle_type ON vehicles(vehicle_type);
CREATE INDEX idx_pricing_tier ON vehicles(pricing_tier);

-- Indices para la tabla 'rentals'
CREATE INDEX idx_rental_status ON rentals(rental_status);
CREATE INDEX idx_rental_dates ON rentals(start_date, end_date); -- Indice compuesto optimizado

-- Indices para la tabla 'customers'
CREATE INDEX idx_customer_created_at ON customers(created_at);
