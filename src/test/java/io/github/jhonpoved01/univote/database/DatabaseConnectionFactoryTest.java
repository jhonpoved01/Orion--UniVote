package io.github.jhonpoved01.univote.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhonpoved01.univote.config.DatabaseConfig;
import org.junit.jupiter.api.Test;

class DatabaseConnectionFactoryTest {

    @Test
    void buildsExpectedJdbcUrlWithoutOpeningConnection() {
        DatabaseConnectionFactory factory = new DatabaseConnectionFactory(config());

        assertEquals(
                "jdbc:mysql://mysql.internal:3307/votacion_universitaria"
                        + "?sslMode=REQUIRED"
                        + "&connectTimeout=5000"
                        + "&socketTimeout=10000"
                        + "&useUnicode=true"
                        + "&characterEncoding=UTF-8"
                        + "&connectionCollation=utf8mb4_unicode_ci",
                factory.jdbcUrl());
    }

    @Test
    void urlContainsConnectionSecurityAndTimeoutSettings() {
        String url = new DatabaseConnectionFactory(config()).jdbcUrl();

        assertTrue(url.contains("sslMode=REQUIRED"));
        assertTrue(url.contains("connectTimeout=5000"));
        assertTrue(url.contains("socketTimeout=10000"));
        assertTrue(url.contains("mysql.internal:3307/votacion_universitaria"));
    }

    @Test
    void urlDoesNotRevealPasswordOrDependOnOperatingSystemPath() {
        String url = new DatabaseConnectionFactory(config()).jdbcUrl();

        assertFalse(url.contains("clave-ultrasecreta"));
        assertFalse(url.contains("password="));
        assertFalse(url.contains("/home/"));
        assertFalse(url.matches(".*[A-Za-z]:\\\\.*"));
    }

    private static DatabaseConfig config() {
        return new DatabaseConfig(
                "mysql.internal",
                3307,
                "votacion_universitaria",
                "usuario",
                "clave-ultrasecreta",
                "REQUIRED",
                5000,
                10000);
    }
}
