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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        CustomerDto finalCustomerDto = new CustomerDto(1L,
                "Test Name",
                "test@example.com",
                "L12345",
                null, null,
                "123456789", "ACTIVO");

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
        CustomerDto customerDtoWithExistingEmail = new CustomerDto(null,
                "Duplicate Name",
                existingEmail,
                "L54321",
                null, null,
                "987654321",
                "ACTIVO");

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

    @Test
    void testCustomerFindById_Succes(){
        Long customerId = 1L;
        Customer customer = new Customer();
        CustomerDto customerDto = new CustomerDto(
                1L,
                "Jhon Whick",
                "badass@gmail.com",
                "PMO 009",
                null, null,
                "7740987",
                "ACTIVO"

        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerMapper.customerToDto(customer)).thenReturn(customerDto);

        CustomerDto result = customerService.findCustomerById(customerId);

        assertNotNull(result);
        assertEquals(customerDto, result);
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerMapper, times(1)).customerToDto(customer);
    }

    @Test
    void testCustomerFindById_NotFound() {
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            customerService.findCustomerById(customerId);
        });

        verify(customerRepository, times(1)).findById(customerId);
        verify(customerMapper, never()).customerToDto(any());
    }

    @Test
    void testFindAllCustomers_Success() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setId(1L);
        Customer customer2 = new Customer();
        customer2.setId(2L);
        List<Customer> customers = Arrays.asList(customer1, customer2);

        CustomerDto customerDto1 = new CustomerDto(1L, "Customer 1",
                "c1@test.com",
                "L1",
                null, null,
                "1",
                "ACTIVO");
        CustomerDto customerDto2 = new CustomerDto(2L,
                "Customer 2",
                "c2@test.com",
                "L2",
                null, null,
                "2", "ACTIVO");
        List<CustomerDto> customerDtos = Arrays.asList(customerDto1, customerDto2);

        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.customerToDto(customer1)).thenReturn(customerDto1);
        when(customerMapper.customerToDto(customer2)).thenReturn(customerDto2);

        // Act
        List<CustomerDto> result = customerService.findAllCustomers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(customerRepository, times(1)).findAll();
        verify(customerMapper, times(2)).customerToDto(any(Customer.class));
    }

    @Test
    void testFindAllCustomers_EmptyList() {
        // Arrange
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CustomerDto> result = customerService.findAllCustomers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(customerRepository, times(1)).findAll();
        verify(customerMapper, never()).customerToDto(any());
    }
}
