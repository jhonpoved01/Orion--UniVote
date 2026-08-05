package io.github.jhonpoved01.univote.service;

import io.github.jhonpoved01.univote.dao.UserDao;
import io.github.jhonpoved01.univote.exception.AuthenticationException;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.model.UserAuthenticationData;
import io.github.jhonpoved01.univote.security.PasswordVerifier;
import io.github.jhonpoved01.univote.security.UserSession;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public final class AuthenticationService {

    public static final String AUTHENTICATION_FAILED_MESSAGE =
            "No fue posible iniciar sesión con las credenciales proporcionadas.";

    private final UserDao userDao;
    private final PasswordVerifier passwordVerifier;

    public AuthenticationService(UserDao userDao, PasswordVerifier passwordVerifier) {
        this.userDao = Objects.requireNonNull(userDao, "userDao no puede ser null");
        this.passwordVerifier = Objects.requireNonNull(
                passwordVerifier, "passwordVerifier no puede ser null");
    }

    public UserSession authenticate(String identifier, char[] password) {
        try {
            String normalizedIdentifier = normalizeIdentifier(identifier);
            if (normalizedIdentifier == null || password == null || password.length == 0) {
                throw authenticationFailed();
            }

            UserAuthenticationData data = userDao.findForAuthentication(normalizedIdentifier)
                    .orElseThrow(AuthenticationService::authenticationFailed);
            if (!data.active() || !passwordVerifier.verify(password, data.passwordHash())) {
                throw authenticationFailed();
            }

            return new UserSession(data.toAuthenticatedUser(), Instant.now());
        } catch (DataAccessException exception) {
            throw new AuthenticationException(AUTHENTICATION_FAILED_MESSAGE, exception);
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    private static String normalizeIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        return identifier.strip();
    }

    private static AuthenticationException authenticationFailed() {
        return new AuthenticationException(AUTHENTICATION_FAILED_MESSAGE);
    }
}
