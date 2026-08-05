package io.github.jhonpoved01.univote.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhonpoved01.univote.config.DatabaseConfig;
import io.github.jhonpoved01.univote.config.DatabaseConfigLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "univote.integration.database", matches = "true")
class DatabaseHealthCheckTest {

    @Test
    void connectsToConfiguredDatabaseAndReportsHealthy() {
        DatabaseConfig config = new DatabaseConfigLoader().load();
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(config);
        DatabaseHealthCheck healthCheck = new DatabaseHealthCheck(connectionFactory);

        assertTrue(healthCheck.isHealthy());
    }
}
