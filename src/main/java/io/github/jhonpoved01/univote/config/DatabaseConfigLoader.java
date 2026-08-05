package io.github.jhonpoved01.univote.config;

import io.github.jhonpoved01.univote.exception.ConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class DatabaseConfigLoader {

    private static final String JVM_CONFIG_PATH = "univote.config.path";
    private static final String ENV_CONFIG_PATH = "UNIVOTE_CONFIG_PATH";
    private static final Path DEFAULT_CONFIG_PATH =
            Path.of("config", "application-local.properties");

    private static final Map<String, String> ENVIRONMENT_KEYS = Map.of(
            "UNIVOTE_DB_HOST", "db.host",
            "UNIVOTE_DB_PORT", "db.port",
            "UNIVOTE_DB_NAME", "db.name",
            "UNIVOTE_DB_USER", "db.user",
            "UNIVOTE_DB_PASSWORD", "db.password",
            "UNIVOTE_DB_SSL_MODE", "db.ssl-mode",
            "UNIVOTE_DB_CONNECT_TIMEOUT_MS", "db.connect-timeout-ms",
            "UNIVOTE_DB_SOCKET_TIMEOUT_MS", "db.socket-timeout-ms");

    private final Map<String, String> environment;
    private final Properties systemProperties;

    public DatabaseConfigLoader() {
        this(System.getenv(), System.getProperties());
    }

    public DatabaseConfigLoader(
            Map<String, String> environment, Properties systemProperties) {
        this.environment = Map.copyOf(environment);
        this.systemProperties = new Properties();
        this.systemProperties.putAll(systemProperties);
    }

    public DatabaseConfig load() {
        Properties values = safeDefaults();
        Path configPath = selectedConfigPath();

        if (Files.exists(configPath)) {
            loadExternalFile(configPath, values);
        }
        applyEnvironment(values);

        return new DatabaseConfig(
                required(values, "db.host"),
                integer(values, "db.port"),
                required(values, "db.name"),
                required(values, "db.user"),
                required(values, "db.password"),
                required(values, "db.ssl-mode"),
                integer(values, "db.connect-timeout-ms"),
                integer(values, "db.socket-timeout-ms"));
    }

    private Path selectedConfigPath() {
        String jvmPath = systemProperties.getProperty(JVM_CONFIG_PATH);
        if (jvmPath != null && !jvmPath.isBlank()) {
            return Path.of(jvmPath.strip());
        }
        String environmentPath = environment.get(ENV_CONFIG_PATH);
        if (environmentPath != null && !environmentPath.isBlank()) {
            return Path.of(environmentPath.strip());
        }
        return DEFAULT_CONFIG_PATH;
    }

    private static Properties safeDefaults() {
        Properties defaults = new Properties();
        defaults.setProperty("db.host", "localhost");
        defaults.setProperty("db.port", "3306");
        defaults.setProperty("db.name", "votacion_universitaria");
        defaults.setProperty("db.ssl-mode", "PREFERRED");
        defaults.setProperty("db.connect-timeout-ms", "5000");
        defaults.setProperty("db.socket-timeout-ms", "10000");
        return defaults;
    }

    private static void loadExternalFile(Path path, Properties target) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            target.load(reader);
        } catch (IOException exception) {
            throw new ConfigurationException(
                    "No fue posible leer el archivo externo de configuración", exception);
        }
    }

    private void applyEnvironment(Properties target) {
        ENVIRONMENT_KEYS.forEach((environmentKey, propertyKey) -> {
            String value = environment.get(environmentKey);
            if (value != null) {
                target.setProperty(propertyKey, value);
            }
        });
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Falta la propiedad obligatoria: " + key);
        }
        return value.strip();
    }

    private static int integer(Properties properties, String key) {
        String value = required(properties, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("La propiedad debe ser un entero: " + key, exception);
        }
    }
}
