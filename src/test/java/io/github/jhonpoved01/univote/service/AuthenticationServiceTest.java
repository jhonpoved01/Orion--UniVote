package io.github.jhonpoved01.univote.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhonpoved01.univote.dao.UserDao;
import io.github.jhonpoved01.univote.exception.AuthenticationException;
import io.github.jhonpoved01.univote.model.UserAuthenticationData;
import io.github.jhonpoved01.univote.security.PasswordVerifier;
import io.github.jhonpoved01.univote.security.UserSession;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthenticationServiceTest {

    private static final char[] EXPECTED_PASSWORD = "ContraseñaDePrueba!".toCharArray();
    private static final char[] CLEARED_PASSWORD = new char[EXPECTED_PASSWORD.length];

    @Test
    void authenticatesSuccessfullyAndNormalizesIdentifierEdges() {
        RecordingUserDao dao = new RecordingUserDao(Optional.of(activeUser("valid-hash")));
        AuthenticationService service = service(dao);

        UserSession session = service.authenticate("  persona@example.test  ", password());

        assertEquals("persona@example.test", dao.lastIdentifier);
        assertEquals(1, session.user().userId());
        assertEquals("ESTUDIANTE", session.user().roleName());
    }

    @Test
    void rejectsMissingUser() {
        assertGenericFailure(service(new RecordingUserDao(Optional.empty())), password());
    }

    @Test
    void rejectsInactiveUser() {
        RecordingUserDao dao = new RecordingUserDao(Optional.of(user(false, "valid-hash")));

        assertGenericFailure(service(dao), password());
    }

    @Test
    void rejectsIncorrectPassword() {
        assertGenericFailure(service(new RecordingUserDao(Optional.of(activeUser("valid-hash")))),
                "IncorrectaDePrueba!".toCharArray());
    }

    @Test
    void rejectsInvalidHash() {
        assertGenericFailure(service(new RecordingUserDao(Optional.of(activeUser("invalid-hash")))),
                password());
    }

    @Test
    void rejectsBlankIdentifier() {
        assertGenericFailure(service(new RecordingUserDao(Optional.of(activeUser("valid-hash")))),
                "   ", password());
    }

    @Test
    void rejectsEmptyPassword() {
        assertGenericFailure(service(new RecordingUserDao(Optional.of(activeUser("valid-hash")))),
                "persona@example.test", new char[0]);
    }

    @Test
    void exposesLoadedPermissionsThroughSession() {
        UserSession session = service(new RecordingUserDao(Optional.of(activeUser("valid-hash"))))
                .authenticate("DOC-1", password());

        assertTrue(session.hasPermission("emitir_voto"));
        assertTrue(session.hasPermission("CONSULTAR_RESULTADOS"));
        assertFalse(session.hasPermission("GESTIONAR_USUARIOS"));
    }

    @Test
    void sessionPermissionsAreImmutable() {
        UserSession session = service(new RecordingUserDao(Optional.of(activeUser("valid-hash"))))
                .authenticate("DOC-1", password());

        assertThrows(UnsupportedOperationException.class,
                () -> session.user().permissions().add("GESTIONAR_USUARIOS"));
    }

    @Test
    void clearsPasswordAfterSuccessfulAttempt() {
        char[] supplied = password();

        service(new RecordingUserDao(Optional.of(activeUser("valid-hash"))))
                .authenticate("DOC-1", supplied);

        assertArrayEquals(CLEARED_PASSWORD, supplied);
    }

    @Test
    void clearsPasswordAfterFailedAttempt() {
        char[] supplied = password();

        assertThrows(AuthenticationException.class,
                () -> service(new RecordingUserDao(Optional.empty()))
                        .authenticate("DOC-1", supplied));

        assertArrayEquals(CLEARED_PASSWORD, supplied);
    }

    @Test
    void allCredentialErrorsUseSamePublicMessage() {
        AuthenticationService missing = service(new RecordingUserDao(Optional.empty()));
        AuthenticationService inactive = service(
                new RecordingUserDao(Optional.of(user(false, "valid-hash"))));
        AuthenticationService incorrect = service(
                new RecordingUserDao(Optional.of(activeUser("valid-hash"))));
        AuthenticationService invalidHash = service(
                new RecordingUserDao(Optional.of(activeUser("invalid-hash"))));

        Set<String> messages = new HashSet<>();
        messages.add(failureMessage(missing, password()));
        messages.add(failureMessage(inactive, password()));
        messages.add(failureMessage(incorrect, "IncorrectaDePrueba!".toCharArray()));
        messages.add(failureMessage(invalidHash, password()));

        assertEquals(Set.of(AuthenticationService.AUTHENTICATION_FAILED_MESSAGE), messages);
    }

    private static AuthenticationService service(UserDao dao) {
        PasswordVerifier verifier = (candidate, hash) ->
                "valid-hash".equals(hash) && Arrays.equals(candidate, EXPECTED_PASSWORD);
        return new AuthenticationService(dao, verifier);
    }

    private static UserAuthenticationData activeUser(String hash) {
        return user(true, hash);
    }

    private static UserAuthenticationData user(boolean active, String hash) {
        return new UserAuthenticationData(
                1, "DOC-1", "Nombre", "Apellido", "persona@example.test",
                hash, active, 2, "ESTUDIANTE",
                Set.of("EMITIR_VOTO", "CONSULTAR_RESULTADOS"));
    }

    private static char[] password() {
        return EXPECTED_PASSWORD.clone();
    }

    private static void assertGenericFailure(AuthenticationService service, char[] supplied) {
        assertGenericFailure(service, "DOC-1", supplied);
    }

    private static void assertGenericFailure(
            AuthenticationService service, String identifier, char[] supplied) {
        AuthenticationException exception = assertThrows(
                AuthenticationException.class, () -> service.authenticate(identifier, supplied));
        assertEquals(AuthenticationService.AUTHENTICATION_FAILED_MESSAGE, exception.getMessage());
    }

    private static String failureMessage(AuthenticationService service, char[] supplied) {
        return assertThrows(AuthenticationException.class,
                () -> service.authenticate("DOC-1", supplied)).getMessage();
    }

    private static final class RecordingUserDao implements UserDao {
        private final Optional<UserAuthenticationData> result;
        private String lastIdentifier;

        private RecordingUserDao(Optional<UserAuthenticationData> result) {
            this.result = result;
        }

        @Override
        public Optional<UserAuthenticationData> findForAuthentication(String identifier) {
            lastIdentifier = identifier;
            return result;
        }
    }
}
