-- V3: Aplica la restriccion NOT NULL a las nuevas columnas de precios.
-- Esto se hace despues del relleno de datos para asegurar la integridad.
ALTER TABLE vehicles MODIFY COLUMN vehicle_type VARCHAR(255) NOT NULL;
ALTER TABLE vehicles MODIFY COLUMN pricing_tier VARCHAR(255) NOT NULL;
