package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public final class JdbcVoteDao implements VoteDao {
    private static final String REGISTER_VOTE_CALL = "{call sp_registrar_voto(?, ?, ?, ?)}";
    private final DatabaseConnectionFactory connectionFactory;

    public JdbcVoteDao(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    @Override
    public String registerVote(int userId, int electionId, int candidacyId) {
        try (Connection connection = connectionFactory.openConnection();
                CallableStatement statement = connection.prepareCall(REGISTER_VOTE_CALL)) {
            statement.setInt(1, userId);
            statement.setInt(2, electionId);
            statement.setInt(3, candidacyId);
            statement.registerOutParameter(4, Types.CHAR);
            statement.execute();
            String verificationCode = statement.getString(4);
            if (verificationCode == null || verificationCode.isBlank()) {
                throw new DataAccessException("No fue posible obtener el comprobante del voto", null);
            }
            return verificationCode;
        } catch (SQLException exception) {
            throw new DataAccessException("No fue posible registrar el voto", exception);
        }
    }
}
