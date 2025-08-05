package com.alquiler.car_rent.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stg")
@TestPropertySource(properties = {

        "DB_HOST=localhost",
        "DB_PORT=3306",
        "DB_USER=root",
        "DB_NAME=stg_database",
        "DB_PASS=diogenes",
        "INIT_ADMIN_USER=Test QA",
        "INIT_ADMIN_EMAIL=testqa@gmail.com",
        "INIT_ADMIN_PASS=test-qa",
        "JWT_SECRET= TMy6jvnM8uXem/ZUkLaMZNFiLODcKF/E5CMGizcp8vNmTILlFSjuKZP45rup83ii72ybBq6Uh5kBGSQe32Pevg=="
    // Si tu application-stg.yaml necesita más variables como usuario y contraseña,
    // añádelas aquí. Por ejemplo:
    // "DB_USER=myuser",
    // "DB_PASSWORD=mypassword"
})
@Transactional
public abstract class AbstractIntegrationTest {
    // Esta clase se deja vacía a propósito.
    // Su única función es contener la configuración para que otras clases la hereden.
}
