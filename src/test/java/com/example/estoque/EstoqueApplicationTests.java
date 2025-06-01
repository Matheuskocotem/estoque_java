package com.example.estoque;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-application.properties")
class EstoqueApplicationTests {

    @Test
    void contextLoads() {
        // Testa se o contexto da aplicação é carregado com sucesso
    }
}
