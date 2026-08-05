package io.github.jhonpoved01.univote.config;

import io.github.jhonpoved01.univote.exception.ConfigurationException;
import java.util.Locale;
import java.util.Set;

public record DatabaseConfig(
        String host,
        int port,
        String databaseName,
        String user,
        String password,
        String sslMode,
        int connectTimeoutMillis,
        int socketTimeoutMillis) {

    private static final Set<String> ALLOWED_SSL_MODES = Set.of(
            "DISABLED", "PREFERRED", "REQUIRED", "VERIFY_CA", "VERIFY_IDENTITY");

    public DatabaseConfig {
        host = required(host, "host");
        databaseName = required(databaseName, "nombre de base de datos");
        user = required(user, "usuario");
        password = requiredPassword(password);
        sslMode = required(sslMode, "modo SSL").toUpperCase(Locale.ROOT);

        if (port < 1 || port > 65_535) {
            throw new ConfigurationException("El puerto de base de datos debe estar entre 1 y 65535");
        }
        if (!databaseName.matches("[A-Za-z0-9_]+")) {
            throw new ConfigurationException("El nombre de base de datos contiene caracteres no permitidos");
        }
        if (host.contains("/") || host.contains("?") || host.contains("#")) {
            throw new ConfigurationException("El host de base de datos contiene caracteres no permitidos");
        }
        if (!ALLOWED_SSL_MODES.contains(sslMode)) {
            throw new ConfigurationException("El modo SSL configurado no es válido");
        }
        if (connectTimeoutMillis <= 0 || socketTimeoutMillis <= 0) {
            throw new ConfigurationException("Los tiempos de espera de base de datos deben ser positivos");
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Falta el valor obligatorio de base de datos: " + field);
        }
        return value.strip();
    }

    private static String requiredPassword(String value) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Falta la contraseña obligatoria de base de datos");
        }
        return value;
    }

    @Override
    public String toString() {
        return "DatabaseConfig[host=" + host
                + ", port=" + port
                + ", databaseName=" + databaseName
                + ", user=" + user
                + ", password=<redacted>"
                + ", sslMode=" + sslMode
                + ", connectTimeoutMillis=" + connectTimeoutMillis
                + ", socketTimeoutMillis=" + socketTimeoutMillis + ']';
    }
}
