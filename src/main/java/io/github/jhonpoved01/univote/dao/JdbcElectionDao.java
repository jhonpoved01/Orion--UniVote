package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.model.Election;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class JdbcElectionDao implements ElectionDao {
    private static final String COLUMNS = """
            id_eleccion, nombre_eleccion, descripcion_eleccion,
            fecha_inicio, fecha_fin, estado
            """;
    private static final String FIND_AVAILABLE = """
            SELECT %s
            FROM elecciones
            WHERE estado = 'ACTIVA'
              AND CURRENT_TIMESTAMP BETWEEN fecha_inicio AND fecha_fin
            ORDER BY fecha_inicio ASC, id_eleccion ASC
            """.formatted(COLUMNS);
    private static final String FIND_BY_ID = """
            SELECT %s
            FROM elecciones
            WHERE id_eleccion = ?
            """.formatted(COLUMNS);
    private static final String FIND_FINISHED = """
            SELECT %s
            FROM elecciones
            WHERE estado = 'FINALIZADA' AND CURRENT_TIMESTAMP >= fecha_fin
            ORDER BY fecha_fin DESC, id_eleccion DESC
            """.formatted(COLUMNS);

    private final DatabaseConnectionFactory connectionFactory;

    public JdbcElectionDao(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    @Override
    public List<Election> findAvailableElections() {
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_AVAILABLE);
                ResultSet resultSet = statement.executeQuery()) {
            List<Election> elections = new ArrayList<>();
            while (resultSet.next()) {
                elections.add(map(resultSet));
            }
            return List.copyOf(elections);
        } catch (SQLException exception) {
            throw dataAccessFailure(exception);
        }
    }

    @Override
    public Optional<Election> findById(int electionId) {
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, electionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw dataAccessFailure(exception);
        }
    }

    @Override
    public List<Election> findFinishedElections() {
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_FINISHED);
                ResultSet resultSet = statement.executeQuery()) {
            List<Election> elections = new ArrayList<>();
            while (resultSet.next()) elections.add(map(resultSet));
            return List.copyOf(elections);
        } catch (SQLException exception) {
            throw dataAccessFailure(exception);
        }
    }

    private static Election map(ResultSet resultSet) throws SQLException {
        return new Election(
                resultSet.getInt("id_eleccion"),
                resultSet.getString("nombre_eleccion"),
                resultSet.getString("descripcion_eleccion"),
                resultSet.getTimestamp("fecha_inicio").toLocalDateTime(),
                resultSet.getTimestamp("fecha_fin").toLocalDateTime(),
                resultSet.getString("estado"));
    }

    private static DataAccessException dataAccessFailure(SQLException cause) {
        return new DataAccessException("No fue posible consultar las elecciones", cause);
    }
}
