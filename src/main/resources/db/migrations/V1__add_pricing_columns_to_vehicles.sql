-- V1: Anade las columnas para el nuevo modelo de precios a la tabla 'vehicles'.
-- Se crean como NULLABLES para permitir la modificacion de la tabla sin errores.
ALTER TABLE vehicles ADD COLUMN vehicle_type VARCHAR(255);
ALTER TABLE vehicles ADD COLUMN pricing_tier VARCHAR(255);
