package io.github.jhonpoved01.univote.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.jhonpoved01.univote.dao.VoteVerificationDao;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.exception.VoteVerificationException;
import io.github.jhonpoved01.univote.model.AuthenticatedUser;
import io.github.jhonpoved01.univote.model.VoteVerification;
import io.github.jhonpoved01.univote.security.UserSession;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VoteVerificationServiceTest {
    private static final String VALID_CODE = "a".repeat(64);

    @Test void returnsRegisteredVerification() {
        VoteVerification expected = new VoteVerification(true, LocalDateTime.now());
        assertSame(expected, service(Optional.of(expected)).verify(session(), VALID_CODE));
    }
    @Test void rejectsUnknownCodeSafely() {
        VoteVerificationException error = assertThrows(VoteVerificationException.class,
                () -> service(Optional.empty()).verify(session(), VALID_CODE));
        assertEquals("No se encontró un voto registrado con este código.", error.getMessage());
    }
    @Test void rejectsNullEmptyWrongLengthAndNonHexWithoutCallingDao() {
        FakeDao dao = new FakeDao(Optional.empty());
        VoteVerificationService service = new VoteVerificationService(dao);
        for (String code : new String[]{null, "", "abc", "g".repeat(64)}) {
            assertThrows(VoteVerificationException.class, () -> service.verify(session(), code));
        }
        assertEquals(0, dao.calls);
    }
    @Test void rejectsNullSession() {
        assertThrows(VoteVerificationException.class,
                () -> service(Optional.empty()).verify(null, VALID_CODE));
    }
    @Test void trimsOnlyExternalWhitespace() {
        FakeDao dao = new FakeDao(Optional.of(new VoteVerification(true, LocalDateTime.now())));
        new VoteVerificationService(dao).verify(session(), "  " + VALID_CODE + "  ");
        assertEquals(VALID_CODE, dao.received);
    }
    @Test void translatesTechnicalFailure() {
        FakeDao dao = new FakeDao(Optional.empty()); dao.failure = new DataAccessException("interno", new Exception());
        VoteVerificationException error = assertThrows(VoteVerificationException.class,
                () -> new VoteVerificationService(dao).verify(session(), VALID_CODE));
        assertEquals("No fue posible verificar el comprobante.", error.getMessage());
        assertNotNull(error.getCause());
    }
    @Test void modelAndStringRevealNoIdentityOrCandidacy() {
        VoteVerification verification = new VoteVerification(true, LocalDateTime.now());
        String text = verification.toString().toLowerCase();
        assertFalse(text.contains("usuario")); assertFalse(text.contains("candidatura"));
        assertFalse(text.contains(VALID_CODE));
    }

    private static VoteVerificationService service(Optional<VoteVerification> result) {
        return new VoteVerificationService(new FakeDao(result));
    }
    private static UserSession session() {
        return new UserSession(new AuthenticatedUser(
                1, "DOC", "Nombre", "Apellido", "persona@example.test",
                2, "ESTUDIANTE", Set.of()), Instant.now());
    }
    private static final class FakeDao implements VoteVerificationDao {
        private final Optional<VoteVerification> result; private int calls; private String received;
        private DataAccessException failure;
        FakeDao(Optional<VoteVerification> result) { this.result = result; }
        public Optional<VoteVerification> verify(String code) {
            calls++; received = code; if (failure != null) throw failure; return result;
        }
    }
}
