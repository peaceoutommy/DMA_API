package dev.tomas.dma.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tomas.dma.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",

        // JWT test properties
        "JWT_SECRET=test-secret-key-for-testing-purposes-only-min-256-bits",
        "JWT_EXPIRATION=3600000",
        "CLOUDINARY_URL=cloudinary://533778951738529:rvKi3p5y7Sw_dF5VbNlWSxBvT68@fichasja",
        "STIPE_PUB_KEY=pk_test_51SSkgDasdadasdafasd123qwedasdasdasd2ewdawsd",
        "STRIPE_PRIV_KEY=sk_test_51SSkgDLsdfsdfsdfadsdasd2qedaxccxzv0",
        "STRIPE_WEBHOOK_PRIV=whsec_55fdsfsdfdsfdsfsdfsdfdfsd33ee318",
})
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected CompanyRepo companyRepo;

    @Autowired
    protected CompanyTypeRepo companyTypeRepo;

    @Autowired
    protected CompanyRoleRepo companyRoleRepo;

    @Autowired
    protected CompanyPermissionRepo companyPermissionRepo;

    @Autowired
    protected UserRepo userRepo;

    @BeforeEach
    public void setUp() {
        userRepo.deleteAll();
        companyRoleRepo.deleteAll();
        companyPermissionRepo.deleteAll();
        companyRepo.deleteAll();
        companyTypeRepo.deleteAll();
    }
}