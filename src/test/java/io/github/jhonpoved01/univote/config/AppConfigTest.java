package io.github.jhonpoved01.univote.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jhonpoved01.univote.exception.ConfigurationException;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    @Test
    void loadsPackagedNonSensitiveConfiguration() {
        AppConfig config = AppConfig.load();

        assertEquals("UniVote", config.applicationName());
        assertEquals("0.1.0", config.applicationVersion());
        assertEquals("UniVote | Participación universitaria", config.windowTitle());
        assertEquals(1280, config.initialWidth());
        assertEquals(800, config.initialHeight());
    }

    @Test
    void rejectsMissingRequiredProperty() {
        Properties properties = validProperties();
        properties.remove("app.name");

        assertThrows(ConfigurationException.class, () -> AppConfig.from(properties));
    }

    @Test
    void rejectsNonNumericWindowDimension() {
        Properties properties = validProperties();
        properties.setProperty("app.window.min-width", "wide");

        assertThrows(ConfigurationException.class, () -> AppConfig.from(properties));
    }

    @Test
    void rejectsInitialWindowSmallerThanMinimum() {
        Properties properties = validProperties();
        properties.setProperty("app.window.initial-width", "800");

        assertThrows(ConfigurationException.class, () -> AppConfig.from(properties));
    }

    private static Properties validProperties() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "UniVote");
        properties.setProperty("app.version", "test");
        properties.setProperty("app.window.title", "UniVote Test");
        properties.setProperty("app.window.min-width", "1024");
        properties.setProperty("app.window.min-height", "680");
        properties.setProperty("app.window.initial-width", "1280");
        properties.setProperty("app.window.initial-height", "800");
        return properties;
    }
}
