-- V2: Rellena los datos de los vehiculos existentes con valores por defecto.
-- ASUNCION: Todos los vehiculos preexistentes se clasifican como 'SEDAN' y 'STANDARD'.
UPDATE vehicles SET
    vehicle_type = 'SEDAN',
    pricing_tier = 'STANDARD'
WHERE vehicle_type IS NULL OR pricing_tier IS NULL;
