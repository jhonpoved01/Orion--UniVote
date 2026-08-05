package io.github.jhonpoved01.univote.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.jhonpoved01.univote.dao.ElectionDao;
import io.github.jhonpoved01.univote.dao.ElectionResultDao;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.exception.ElectionResultsException;
import io.github.jhonpoved01.univote.model.*;
import io.github.jhonpoved01.univote.security.UserSession;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class ElectionResultsServiceTest {
    @Test void returnsResultsForFinishedElectionAndKeepsBigDecimal() {
        Fixtures f = fixtures(finished());
        List<ElectionResult> results = f.service.getPublishedResults(session("CONSULTAR_RESULTADOS"), 1);
        assertEquals(new BigDecimal("50.00"), results.getFirst().percentage());
    }
    @Test void rejectsActiveAndProgrammedElection() {
        for (String status : List.of("ACTIVA", "PROGRAMADA")) {
            Fixtures f = fixtures(election(status, LocalDateTime.now().minusHours(1)));
            assertThrows(ElectionResultsException.class,
                    () -> f.service.getPublishedResults(session("CONSULTAR_RESULTADOS"), 1));
            assertEquals(0, f.resultDao.calls);
        }
    }
    @Test void rejectsFinishedElectionBeforeEndDate() {
        Fixtures f = fixtures(election("FINALIZADA", LocalDateTime.now().plusHours(1)));
        assertThrows(ElectionResultsException.class,
                () -> f.service.getPublishedResults(session("CONSULTAR_RESULTADOS"), 1));
        assertEquals(0, f.resultDao.calls);
    }
    @Test void rejectsMissingPermissionAndNullSession() {
        assertThrows(ElectionResultsException.class,
                () -> fixtures(finished()).service.getPublishedResults(session(), 1));
        assertThrows(ElectionResultsException.class,
                () -> fixtures(finished()).service.getPublishedResults(null, 1));
    }
    @Test void roleAloneDoesNotAuthorize() {
        UserSession session = session();
        assertEquals("ESTUDIANTE", session.user().roleName());
        assertThrows(ElectionResultsException.class,
                () -> fixtures(finished()).service.getPublishedResults(session, 1));
    }
    @Test void doesNotCallResultDaoWhenPolicyFails() {
        Fixtures f = fixtures(election("ACTIVA", LocalDateTime.now().plusHours(1)));
        assertThrows(ElectionResultsException.class,
                () -> f.service.getPublishedResults(session("CONSULTAR_RESULTADOS"), 1));
        assertEquals(0, f.resultDao.calls);
    }
    @Test void translatesTechnicalError() {
        Fixtures f = fixtures(finished());
        f.resultDao.failure = new DataAccessException("interno", new Exception());
        ElectionResultsException error = assertThrows(ElectionResultsException.class,
                () -> f.service.getPublishedResults(session("CONSULTAR_RESULTADOS"), 1));
        assertEquals("No fue posible consultar los resultados.", error.getMessage());
    }
    @Test void listsOnlyPublishableElectionsFromDao() {
        Fixtures f = fixtures(finished());
        assertEquals(1, f.service.getPublishableElections(
                session("CONSULTAR_RESULTADOS")).size());
    }

    private static Fixtures fixtures(Election election) {
        FakeElectionDao elections = new FakeElectionDao(election);
        FakeResultDao results = new FakeResultDao();
        return new Fixtures(new ElectionResultsService(elections, results), results);
    }
    private static Election finished() { return election("FINALIZADA", LocalDateTime.now().minusHours(1)); }
    private static Election election(String status, LocalDateTime end) {
        return new Election(1, "Elección", "Descripción", end.minusHours(2), end, status);
    }
    private static UserSession session(String... permissions) {
        return new UserSession(new AuthenticatedUser(
                1, "DOC", "Nombre", "Apellido", "persona@example.test",
                2, "ESTUDIANTE", Set.of(permissions)), Instant.now());
    }
    private record Fixtures(ElectionResultsService service, FakeResultDao resultDao) {}
    private static final class FakeElectionDao implements ElectionDao {
        private final Election election;
        FakeElectionDao(Election election) { this.election = election; }
        public List<Election> findAvailableElections() { return List.of(); }
        public List<Election> findFinishedElections() { return List.of(election); }
        public Optional<Election> findById(int id) { return Optional.of(election); }
    }
    private static final class FakeResultDao implements ElectionResultDao {
        private int calls; private DataAccessException failure;
        public List<ElectionResult> findByElectionId(int id) {
            calls++; if (failure != null) throw failure;
            return List.of(new ElectionResult(1, "Elección", "FINALIZADA", 2, 1,
                    "Persona", "Propuesta", 3, new BigDecimal("50.00")));
        }
    }
}
