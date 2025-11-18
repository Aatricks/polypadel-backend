package com.polypadel.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// Removed unused imports

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
public abstract class PostgresTest {
    // no-op: container lifecycle managed by Testcontainers with automatic skipping when Docker is unavailable

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("polypadel_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("security.jwt.secret", () -> "dGVzdGluZy1qd3Qtc2VjcmV0LXNlY3JldC1qd3Qtc2VjcmV0LXNlY3JldA==");
        registry.add("security.jwt.expHours", () -> 24);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }
}
