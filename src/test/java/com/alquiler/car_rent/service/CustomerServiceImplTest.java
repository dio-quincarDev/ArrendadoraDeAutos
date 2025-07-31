package com.alquiler.car_rent.service;

import com.alquiler.car_rent.commons.dtos.CustomerDto;
import com.alquiler.car_rent.commons.entities.Customer;
import com.alquiler.car_rent.commons.mappers.CustomerMapper;
import com.alquiler.car_rent.exceptions.BadRequestException;
import com.alquiler.car_rent.exceptions.NotFoundException;
import com.alquiler.car_rent.repositories.CustomerRepository;
import com.alquiler.car_rent.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository, customerMapper);
    }

    @Test
    void testCreateCustomer_Success() {
        // Arrange: Configurar el comportamiento de los mocks
        CustomerDto customerDtoToSave = new CustomerDto(null, "Test Name", "test@example.com", "L12345", null, null, "123456789", "ACTIVO");
        Customer mappedCustomer = new Customer(); // Objeto intermedio
        Customer savedCustomer = new Customer(); // Objeto que simula el guardado en DB
        savedCustomer.setId(1L);
        savedCustomer.setEmail("test@example.com");

        // DTO que simula el resultado final
        CustomerDto finalCustomerDto = new CustomerDto(1L, "Test Name", "test@example.com", "L12345", null, null, "123456789", "ACTIVO");

        when(customerRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(customerMapper.dtoToCustomer(any(CustomerDto.class))).thenReturn(mappedCustomer);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(customerMapper.customerToDto(any(Customer.class))).thenReturn(finalCustomerDto);

        // Act: Ejecutar el método que se está probando
        CustomerDto result = customerService.createCustomer(customerDtoToSave);

        // Assert: Verificar el resultado
        assertNotNull(result);
        assertEquals("test@example.com", result.email());
    }

    @Test
    void testCreateCustomer_ThrowsBadRequestException_WhenEmailExists() {
        // Arrange
        String existingEmail = "duplicate@example.com";
        CustomerDto customerDtoWithExistingEmail = new CustomerDto(null, "Duplicate Name", existingEmail, "L54321", null, null, "987654321", "ACTIVO");

        when(customerRepository.existsByEmail(existingEmail)).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            customerService.createCustomer(customerDtoWithExistingEmail);
        });
    }

    @Test
    void testUpdateCustomerExitoso(){
        Long customerId = 1L;
        CustomerDto updateInfo = new CustomerDto(
                customerId,
                "Juan Perez Updated",
                "juan.prz.new@example.com",
                "LT 009",
                null, null,
                "7757265",
                "ACTIVO"
        );
                Customer existingCustomer = new Customer();
                existingCustomer.setId(customerId);
                existingCustomer.setName("Luis Perez");
                existingCustomer.setEmail("juan.prz.original@example.com");

                when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
                when(customerRepository.save(any(Customer.class))).thenReturn(existingCustomer);
                when(customerMapper.customerToDto(any(Customer.class))).thenReturn(updateInfo);

                CustomerDto result = customerService.updateCustomer(customerId, updateInfo);

                assertNotNull(result);
                assertEquals(updateInfo.name(), result.name());
                assertEquals(updateInfo.email(), result.email());

    }

    @Test
    void testeUpdateCustomerNoExitoso(){
        Long noExitosoCustomerId = 99L;
        CustomerDto customerInfo = new CustomerDto(
                 noExitosoCustomerId,
                "No Name",
                "no@email.com",
                "000", null,
                null,
                "+775725",
                "ACTIVO"
        );

        when(customerRepository.findById(noExitosoCustomerId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, ()->{
            customerService.updateCustomer(noExitosoCustomerId, customerInfo);
        });
    }

    @Test
    void testDeleteCustomer(){
        Long customerId = 1L;

        when(customerRepository.existsById(customerId)).thenReturn(true);

        customerService.deleteCustomer(customerId);

        verify(customerRepository, times(1)).deleteById(customerId);
    }
}
