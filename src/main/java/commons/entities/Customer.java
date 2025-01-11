package commons.entities;

import java.time.LocalDateTime;

import commons.enums.CustomerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "Nombre obligatorio")
	private String name;
	
	@NotBlank(message = "Correo Obligatorio")
	@Email(message = "El Correo debe ser Valido")
	private String email;
	
	@NotBlank(message = "Numero de identificacion Obligatorio")
	@Column(name = "number_id", nullable = false, unique = true, length = 20)
	private String numberId;
	

	@NotBlank(message = "Numero de Telefono Obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{10,12}$", message = "Formato de teléfono inválido")
	private String phone;
	
	@Enumerated(EnumType.STRING)
	private CustomerStatus customerStatus;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = this.createdAt;
		
		if(this.customerStatus == null) {
			this.customerStatus = CustomerStatus.ACTIVE;
		}
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
	

}
