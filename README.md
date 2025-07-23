# Sistema de Gestión de Alquiler de Vehículos

<div align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot 3.2">
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL 8">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge" alt="License: MIT">
</div>

---

**Car Rent** es una API RESTful robusta y escalable para la gestión integral de un negocio de alquiler de vehículos. Construida con tecnologías modernas de Java, esta API demuestra las mejores prácticas en arquitectura de software, seguridad y diseño de sistemas.

## ✨ Características Principales

- **Gestión de Entidades:** Operaciones CRUD completas para Vehículos, Clientes y Alquileres.
- **Seguridad Avanzada:** Autenticación basada en JWT y autorización por roles (`ADMIN`, `USER`) con Spring Security.
- **Sistema de Reportes Dinámicos:** Generación de reportes en múltiples formatos (PDF, Excel) y visualización de métricas clave.
- **Notificaciones Multi-proveedor:** Sistema de envío de SMS flexible, con capacidad para cambiar entre proveedores (ej. Twilio, Vonage) mediante configuración.
- **Alertas en Tiempo Real:** Notificaciones a administradores a través de WebSockets sobre eventos importantes (ej. alquileres por vencer).
- **Validación de Datos:** Reglas de validación a nivel de API para garantizar la integridad de los datos de entrada.
- **Manejo de Transacciones:** Uso de `@Transactional` para asegurar la consistencia de los datos en la base de datos.
- **Documentación de API:** Documentación interactiva y automática con OpenAPI 3 (Swagger).

## 🏛️ Arquitectura y Diseño

Este proyecto está diseñado siguiendo principios de software de alta calidad para garantizar su mantenibilidad y escalabilidad.

- **Arquitectura por Capas:** Clara separación de responsabilidades:
    - `Controllers`: Exposición de la API REST.
    - `Services`: Orquestación y lógica de negocio.
    - `Repositories`: Acceso y persistencia de datos (JPA).
- **Patrón DTO (Data Transfer Object):** Se utiliza MapStruct para desacoplar la representación de la API de las entidades de la base de datos, mejorando la seguridad y la flexibilidad.
- **Inyección de Dependencias:** Gestionada por Spring, promoviendo un bajo acoplamiento.
- **Manejo de Excepciones Centralizado:** Un `@ControllerAdvice` global proporciona respuestas de error consistentes y limpias.
- **Patrón Strategy para Servicios Externos:** La implementación del servicio de SMS utiliza un patrón de estrategia para seleccionar dinámicamente el proveedor (Twilio/Vonage), lo que demuestra un diseño de software flexible y extensible.

## 🛠️ Stack Tecnológico

| Área                | Tecnología                                       |
| ------------------- | ------------------------------------------------ |
| **Backend**         | Java 21, Spring Boot 3.2, Spring Web             |
| **Base de Datos**   | MySQL, Spring Data JPA (Hibernate)               |
| **Migraciones**     | Flyway                                           |
| **Seguridad**       | Spring Security, JWT (jjwt)                      |
| **Documentación**   | Springdoc (OpenAPI 3)                            |
| **Mapeo de Objetos**| MapStruct                                        |
| **Comunicaciones**  | Vonage/Twilio API, Spring WebSocket              |
| **Reportes**        | iText (PDF), Apache POI (Excel), JFreeChart      |
| **Desarrollo**      | Lombok, Maven                                    |

## 🚀 Cómo Empezar

### Prerrequisitos

- JDK 21 o superior
- Maven 3.8+
- MySQL 8.0+
- Una cuenta en un proveedor de SMS (ej. Vonage)

### Configuración

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/tu-usuario/car_rent.git
    cd car_rent
    ```

2.  **Configurar la base de datos:**
    - Abre una terminal de MySQL y crea la base de datos:
      ```sql
      CREATE DATABASE car_rent;
      ```

3.  **Configurar las variables de entorno:**
    - Navega a `src/main/resources/` y renombra el archivo `application.yaml.example` a `application.yaml`.
    - Edita `application.yaml` con tus credenciales:
      ```yaml
      spring:
        datasource:
          url: jdbc:mysql://localhost:3306/car_rent
          username: TU_USUARIO_MYSQL
          password: TU_PASSWORD_MYSQL
      
      jwt:
        secret: TU_CLAVE_SECRETA_JWT_DE_256_BITS_O_MAS
      
      sms:
        provider: vonage # o twilio
      
      vonage:
        api-key: TU_VONAGE_API_KEY
        api-secret: TU_VONAGE_API_SECRET
        from-number: "CarRentPA" # Tu Sender ID
      
      twilio:
        enabled: false
        account-sid: TU_TWILIO_SID
        auth-token: TU_TWILIO_TOKEN
        phone-number: TU_NUMERO_TWILIO
      ```

### Ejecución

- **Desde la línea de comandos con Maven:**
  ```bash
  ./mvnw spring-boot:run
  ```
- La API estará disponible en `http://localhost:8080`.
- La documentación de Swagger UI estará en `http://localhost:8080/swagger-ui.html`.

## 📄 Licencia

Este proyecto está licenciado bajo la **Licencia MIT**. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---
<div align="center">
  Desarrollado con ❤️ por Diogenes Quintero
</div>