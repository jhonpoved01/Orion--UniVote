package io.github.jhonpoved01.univote.database;

import io.github.jhonpoved01.univote.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public final class DatabaseConnectionFactory {

    private final DatabaseConfig config;
    private final String jdbcUrl;

    public DatabaseConnectionFactory(DatabaseConfig config) {
        this.config = Objects.requireNonNull(config, "config no puede ser null");
        this.jdbcUrl = buildJdbcUrl(config);
    }

    public Connection openConnection() throws SQLException {
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("user", config.user());
        connectionProperties.setProperty("password", config.password());
        return DriverManager.getConnection(jdbcUrl, connectionProperties);
    }

    String jdbcUrl() {
        return jdbcUrl;
    }

    private static String buildJdbcUrl(DatabaseConfig config) {
        String host = formatHost(config.host());
        return "jdbc:mysql://" + host + ':' + config.port() + '/' + config.databaseName()
                + "?sslMode=" + config.sslMode()
                + "&connectTimeout=" + config.connectTimeoutMillis()
                + "&socketTimeout=" + config.socketTimeoutMillis()
                + "&useUnicode=true"
                + "&characterEncoding=UTF-8"
                + "&connectionCollation=utf8mb4_unicode_ci";
    }

    private static String formatHost(String host) {
        if (host.indexOf(':') >= 0 && !(host.startsWith("[") && host.endsWith("]"))) {
            return '[' + host + ']';
        }
        return host;
    }
}
