package commons.enums;

public enum VehicleStatus {
	AVAILABLE("Disponible"),
	RENTED("Alquilado"),
	MAINTENANCE("En Mantenimiento"),
	OUT_OF_SERVICE("Fuera de Servicio");
	
	private final String description;
	
	VehicleStatus(String description){
		this.description = description;
		
	}
	
	public String getDescription() {
		return description;
	}

}
