package io.github.jhonpoved01.univote.config;

import io.github.jhonpoved01.univote.exception.ConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public record AppConfig(
        String applicationName,
        String applicationVersion,
        String windowTitle,
        int minimumWidth,
        int minimumHeight,
        int initialWidth,
        int initialHeight) {

    private static final String RESOURCE_NAME =
            "/io/github/jhonpoved01/univote/application.properties";

    public static AppConfig load() {
        try (InputStream input = AppConfig.class.getResourceAsStream(RESOURCE_NAME)) {
            if (input == null) {
                throw new ConfigurationException(
                        "No se encontró la configuración no sensible de la aplicación");
            }
            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(reader);
                return from(properties);
            }
        } catch (IOException exception) {
            throw new ConfigurationException("No fue posible leer la configuración", exception);
        }
    }

    public static AppConfig from(Properties properties) {
        Objects.requireNonNull(properties, "properties no puede ser null");

        String applicationName = required(properties, "app.name");
        String applicationVersion = required(properties, "app.version");
        String windowTitle = required(properties, "app.window.title");
        int minimumWidth = positiveInteger(properties, "app.window.min-width");
        int minimumHeight = positiveInteger(properties, "app.window.min-height");
        int initialWidth = positiveInteger(properties, "app.window.initial-width");
        int initialHeight = positiveInteger(properties, "app.window.initial-height");

        if (initialWidth < minimumWidth || initialHeight < minimumHeight) {
            throw new ConfigurationException(
                    "El tamaño inicial de la ventana no puede ser menor que su tamaño mínimo");
        }

        return new AppConfig(
                applicationName,
                applicationVersion,
                windowTitle,
                minimumWidth,
                minimumHeight,
                initialWidth,
                initialHeight);
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Falta la propiedad obligatoria: " + key);
        }
        return value.strip();
    }

    private static int positiveInteger(Properties properties, String key) {
        String value = required(properties, key);
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new ConfigurationException("La propiedad debe ser positiva: " + key);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("La propiedad debe ser un entero: " + key, exception);
        }
    }
}
