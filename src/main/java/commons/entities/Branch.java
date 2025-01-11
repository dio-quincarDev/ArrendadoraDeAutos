package commons.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "branches")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Branch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "Nombre de la Sucursal Obligatorio")
	private String name;
	
	@NotBlank(message = "Ubicacion de la Sucursal obligatoria")
	private String addres; 
	

}
