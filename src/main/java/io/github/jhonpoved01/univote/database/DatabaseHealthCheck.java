package io.github.jhonpoved01.univote.database;

import io.github.jhonpoved01.univote.exception.DatabaseConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class DatabaseHealthCheck {

    private static final String HEALTH_QUERY = "SELECT 1";

    private final DatabaseConnectionFactory connectionFactory;

    public DatabaseHealthCheck(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(
                connectionFactory, "connectionFactory no puede ser null");
    }

    public boolean isHealthy() {
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(HEALTH_QUERY);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) == 1;
        } catch (SQLException exception) {
            throw new DatabaseConnectionException(
                    "No fue posible verificar la disponibilidad de la base de datos", exception);
        }
    }
}
