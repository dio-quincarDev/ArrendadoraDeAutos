# Car Rent API 🚗

![Java](https://img.shields.io/badge/Java-21-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.0-green?logo=springboot&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql&logoColor=white)

## Descripción 📄

**Car Rent API** es un sistema backend robusto para la gestión integral de alquiler de vehículos, construido con **Spring Boot**. Además de las funcionalidades esenciales para administrar vehículos, clientes y reservas, ahora incluye un completo módulo de **reportes** y un **dashboard de administración** para la visualización de métricas clave. La API está totalmente documentada con **Swagger** para una fácil integración y exploración.

## Funcionalidades ✅

- **Vehículos:** Registro, actualización, consulta y cambio de estado de la flota.
- **Clientes:** Gestión completa de la información de los clientes (registro, consulta, actualización y eliminación).
- **Reservas:** Creación, cancelación y finalización de los procesos de alquiler.
- **Reportes:**
    - Generación de reportes personalizables por período.
    - Exportación en múltiples formatos: **PDF**, **JSON**, **Excel**, y generación de **gráficos** (PNG/SVG - implementación en curso).
    - Tipos de reportes: Resumen de alquileres, vehículos más alquilados, etc.
- **Dashboard de Administración:**
    - **Métricas Clave:** Visualización de información importante como total de alquileres, ingresos totales, vehículos únicos alquilados, vehículo más alquilado y nuevos clientes.
    - **Tendencias de Alquileres:** Seguimiento de las tendencias de alquileres a lo largo del tiempo (diario, semanal, mensual, etc.).
    - **Filtrado por Período:** Capacidad de filtrar las métricas por rangos de fechas personalizados.

## Requisitos Previos 🚀

1. **JDK 21**: Entorno de ejecución necesario para la aplicación Java.
2. **MySQL**: Base de datos relacional con el esquema `arrendadora` configurado.

## Endpoints Principales 🌐

La API sigue una estructura RESTful. Algunos de los endpoints principales incluyen:

- `/v1/vehicles`: Para la gestión de vehículos.
- `/v1/customers`: Para la gestión de clientes.
- `/v1/rentals`: Para la gestión de reservas.
- `/v1/reports`:
    - `/`: Para la vista de generación de reportes (requiere autenticación de administrador).
    - `/export`: Para descargar reportes en diferentes formatos.
    - `/metrics/total-rentals`: Obtiene el total de alquileres.
    - `/metrics/total-revenue`: Obtiene los ingresos totales.
    - `/metrics/unique-vehicles`: Obtiene el número de vehículos únicos alquilados.
    - `/metrics/most-rented-vehicle`: Obtiene el vehículo más alquilado.
    - `/metrics/new-customers`: Obtiene el número de nuevos clientes.
    - `/metrics/rental-trends`: Obtiene las tendencias de alquileres.

## Documentación de la API 🛠️

La documentación interactiva de la API está disponible a través de **Swagger UI**. 
