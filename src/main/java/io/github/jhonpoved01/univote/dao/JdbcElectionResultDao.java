package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.model.ElectionResult;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JdbcElectionResultDao implements ElectionResultDao {
    private static final String RESULTS_CALL = "{call sp_consultar_resultados(?)}";
    private final DatabaseConnectionFactory connectionFactory;

    public JdbcElectionResultDao(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    @Override
    public List<ElectionResult> findByElectionId(int electionId) {
        try (Connection connection = connectionFactory.openConnection();
                CallableStatement statement = connection.prepareCall(RESULTS_CALL)) {
            statement.setInt(1, electionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ElectionResult> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(new ElectionResult(
                            resultSet.getInt("id_eleccion"),
                            resultSet.getString("nombre_eleccion"),
                            resultSet.getString("estado_eleccion"),
                            resultSet.getInt("id_candidatura"),
                            resultSet.getInt("numero_lista"),
                            resultSet.getString("nombre_candidato"),
                            resultSet.getString("titulo_propuesta"),
                            resultSet.getLong("total_votos"),
                            resultSet.getBigDecimal("porcentaje_votos")));
                }
                return List.copyOf(results);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("No fue posible consultar los resultados", exception);
        }
    }
}
