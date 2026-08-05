package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.model.VoteVerification;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public final class JdbcVoteVerificationDao implements VoteVerificationDao {
    private static final String VERIFY_CALL = "{call sp_verificar_voto(?)}";
    private final DatabaseConnectionFactory connectionFactory;

    public JdbcVoteVerificationDao(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory);
    }

    @Override
    public Optional<VoteVerification> verify(String verificationCode) {
        try (Connection connection = connectionFactory.openConnection();
                CallableStatement statement = connection.prepareCall(VERIFY_CALL)) {
            statement.setString(1, verificationCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DataAccessException("No fue posible verificar el comprobante", null);
                }
                boolean registered = "VOTO REGISTRADO".equals(resultSet.getString("resultado"));
                if (!registered) {
                    return Optional.empty();
                }
                return Optional.of(new VoteVerification(
                        true, resultSet.getTimestamp("fecha_registro").toLocalDateTime()));
            }
        } catch (SQLException exception) {
            if ("45000".equals(exception.getSQLState())) {
                return Optional.empty();
            }
            throw new DataAccessException("No fue posible verificar el comprobante", exception);
        }
    }
}
