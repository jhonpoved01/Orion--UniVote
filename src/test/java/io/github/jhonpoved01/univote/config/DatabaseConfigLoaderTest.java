package io.github.jhonpoved01.univote.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jhonpoved01.univote.exception.ConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseConfigLoaderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsUtf8FromTemporaryFile() throws IOException {
        Path configFile = writeConfig("db.user=josé\n");

        DatabaseConfig config = loader(configFile, Map.of()).load();

        assertEquals("josé", config.user());
        assertEquals("votacion_universitaria", config.databaseName());
    }

    @Test
    void simulatedEnvironmentOverridesFile() throws IOException {
        Path configFile = writeConfig("");
        Map<String, String> environment = new HashMap<>();
        environment.put("UNIVOTE_DB_HOST", "mysql-docker");
        environment.put("UNIVOTE_DB_PORT", "3307");
        environment.put("UNIVOTE_DB_NAME", "votacion_pruebas");
        environment.put("UNIVOTE_DB_USER", "usuario_entorno");
        environment.put("UNIVOTE_DB_PASSWORD", "clave-entorno");
        environment.put("UNIVOTE_DB_SSL_MODE", "REQUIRED");
        environment.put("UNIVOTE_DB_CONNECT_TIMEOUT_MS", "7000");
        environment.put("UNIVOTE_DB_SOCKET_TIMEOUT_MS", "12000");

        DatabaseConfig config = loader(configFile, environment).load();

        assertEquals("mysql-docker", config.host());
        assertEquals(3307, config.port());
        assertEquals("usuario_entorno", config.user());
        assertEquals("REQUIRED", config.sslMode());
    }

    @Test
    void jvmPropertySelectsExternalPathBeforeEnvironmentPath() throws IOException {
        Path jvmFile = writeNamedConfig(
                "jvm.properties", validConfig() + "db.host=jvm-host\n");
        Path environmentFile = writeNamedConfig(
                "environment.properties", validConfig() + "db.host=env-host\n");
        Properties jvmProperties = new Properties();
        jvmProperties.setProperty("univote.config.path", jvmFile.toString());
        Map<String, String> environment = Map.of(
                "UNIVOTE_CONFIG_PATH", environmentFile.toString());

        DatabaseConfig config = new DatabaseConfigLoader(environment, jvmProperties).load();

        assertEquals("jvm-host", config.host());
    }

    @Test
    void rejectsNonNumericPort() throws IOException {
        assertInvalid("db.port=not-a-number\n");
    }

    @Test
    void rejectsPortOutsideRange() throws IOException {
        assertInvalid("db.port=65536\n");
    }

    @Test
    void rejectsMissingRequiredUser() throws IOException {
        assertInvalid("db.user=\n");
    }

    @Test
    void rejectsMissingPassword() throws IOException {
        assertInvalid("db.password=\n");
    }

    @Test
    void rejectsZeroTimeout() throws IOException {
        assertInvalid("db.connect-timeout-ms=0\n");
    }

    @Test
    void rejectsNegativeTimeout() throws IOException {
        assertInvalid("db.socket-timeout-ms=-1\n");
    }

    @Test
    void rejectsInvalidSslMode() throws IOException {
        assertInvalid("db.ssl-mode=INSECURE\n");
    }

    @Test
    void stringRepresentationRedactsPassword() throws IOException {
        DatabaseConfig config = loader(writeConfig(""), Map.of()).load();

        assertFalse(config.toString().contains("clave-prueba"));
        assertFalse(config.toString().contains("db.password"));
    }

    private void assertInvalid(String override) throws IOException {
        Path configFile = writeConfig(override);
        DatabaseConfigLoader configLoader = loader(configFile, Map.of());
        assertThrows(ConfigurationException.class, configLoader::load);
    }

    private DatabaseConfigLoader loader(Path path, Map<String, String> environment) {
        Properties jvmProperties = new Properties();
        jvmProperties.setProperty("univote.config.path", path.toString());
        return new DatabaseConfigLoader(environment, jvmProperties);
    }

    private Path writeConfig(String override) throws IOException {
        return writeNamedConfig("application-local.properties", validConfig() + override);
    }

    private Path writeNamedConfig(String name, String content) throws IOException {
        Path path = temporaryDirectory.resolve(name);
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    private static String validConfig() {
        return "db.host=localhost\n"
                + "db.port=3306\n"
                + "db.name=votacion_universitaria\n"
                + "db.user=usuario-prueba\n"
                + "db.password=clave-prueba\n"
                + "db.ssl-mode=PREFERRED\n"
                + "db.connect-timeout-ms=5000\n"
                + "db.socket-timeout-ms=10000\n";
    }
}
