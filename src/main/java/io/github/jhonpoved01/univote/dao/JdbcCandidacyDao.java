package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.model.Candidacy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JdbcCandidacyDao implements CandidacyDao {
    private static final String FIND_BY_ELECTION = """
            SELECT ca.id_candidatura, ca.id_eleccion, ca.numero_lista,
                   CONCAT_WS(' ', u.nombres, u.apellidos) AS nombre_candidato,
                   c.titulo_propuesta, c.propuesta
            FROM candidaturas ca
            INNER JOIN candidatos c ON c.id_candidato = ca.id_candidato AND c.estado = TRUE
            INNER JOIN usuarios u ON u.id_usuario = c.id_usuario AND u.estado = TRUE
            WHERE ca.id_eleccion = ? AND ca.estado = TRUE
            ORDER BY ca.numero_lista ASC
            """;

    private final DatabaseConnectionFactory connectionFactory;

    public JdbcCandidacyDao(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    @Override
    public List<Candidacy> findByElectionId(int electionId) {
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ELECTION)) {
            statement.setInt(1, electionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Candidacy> candidacies = new ArrayList<>();
                while (resultSet.next()) {
                    candidacies.add(new Candidacy(
                            resultSet.getInt("id_candidatura"),
                            resultSet.getInt("id_eleccion"),
                            resultSet.getInt("numero_lista"),
                            resultSet.getString("nombre_candidato"),
                            resultSet.getString("titulo_propuesta"),
                            resultSet.getString("propuesta")));
                }
                return List.copyOf(candidacies);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("No fue posible consultar las candidaturas", exception);
        }
    }
}
