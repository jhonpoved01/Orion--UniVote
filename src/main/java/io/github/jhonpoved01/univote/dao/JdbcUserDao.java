package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.model.UserAuthenticationData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class JdbcUserDao implements UserDao {

    private static final String FIND_FOR_AUTHENTICATION = """
            SELECT u.id_usuario, u.documento, u.nombres, u.apellidos, u.correo,
                   u.password_hash, u.estado, r.id_rol, r.nombre AS nombre_rol,
                   p.nombre AS nombre_permiso
            FROM usuarios u
            INNER JOIN roles r ON r.id_rol = u.id_rol AND r.estado = TRUE
            LEFT JOIN roles_permisos rp ON rp.id_rol = r.id_rol
            LEFT JOIN permisos p ON p.id_permiso = rp.id_permiso AND p.estado = TRUE
            WHERE u.documento = ? OR u.correo = ?
            ORDER BY p.nombre
            """;

    private final DatabaseConnectionFactory connectionFactory;

    public JdbcUserDao(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(
                connectionFactory, "connectionFactory no puede ser null");
    }

    @Override
    public Optional<UserAuthenticationData> findForAuthentication(String identifier) {
        try (Connection connection = connectionFactory.openConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_FOR_AUTHENTICATION)) {
            statement.setString(1, identifier);
            statement.setString(2, identifier);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapAuthenticationData(resultSet);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("No fue posible acceder a los datos de autenticación", exception);
        }
    }

    private static Optional<UserAuthenticationData> mapAuthenticationData(ResultSet resultSet)
            throws SQLException {
        if (!resultSet.next()) {
            return Optional.empty();
        }

        int userId = resultSet.getInt("id_usuario");
        String document = resultSet.getString("documento");
        String firstNames = resultSet.getString("nombres");
        String lastNames = resultSet.getString("apellidos");
        String email = resultSet.getString("correo");
        String passwordHash = resultSet.getString("password_hash");
        boolean active = resultSet.getBoolean("estado");
        int roleId = resultSet.getInt("id_rol");
        String roleName = resultSet.getString("nombre_rol");
        Set<String> permissions = new LinkedHashSet<>();

        do {
            if (resultSet.getInt("id_usuario") != userId) {
                throw new SQLException("El identificador de autenticación no es unívoco");
            }
            String permission = resultSet.getString("nombre_permiso");
            if (permission != null) {
                permissions.add(permission);
            }
        } while (resultSet.next());

        return Optional.of(new UserAuthenticationData(
                userId, document, firstNames, lastNames, email, passwordHash, active,
                roleId, roleName, permissions));
    }
}
