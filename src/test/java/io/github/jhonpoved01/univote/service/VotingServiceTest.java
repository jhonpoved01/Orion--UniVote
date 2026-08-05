package io.github.jhonpoved01.univote.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhonpoved01.univote.dao.CandidacyDao;
import io.github.jhonpoved01.univote.dao.ElectionDao;
import io.github.jhonpoved01.univote.dao.VoteDao;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.exception.VotingException;
import io.github.jhonpoved01.univote.model.AuthenticatedUser;
import io.github.jhonpoved01.univote.model.Candidacy;
import io.github.jhonpoved01.univote.model.Election;
import io.github.jhonpoved01.univote.model.VoteReceipt;
import io.github.jhonpoved01.univote.security.UserSession;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VotingServiceTest {
    private final Election activeElection = election(1, true);
    private final Candidacy candidacy = new Candidacy(
            10, 1, 3, "Persona Candidata", "Propuesta", "Descripción de prueba");

    @Test void listsElectionsForAuthorizedSession() {
        Fixtures fixtures = fixtures();
        assertEquals(List.of(activeElection),
                fixtures.service.getAvailableElections(sessionWith("EMITIR_VOTO")));
    }

    @Test void rejectsSessionWithoutPermission() {
        VotingException error = assertThrows(VotingException.class,
                () -> fixtures().service.getAvailableElections(sessionWith()));
        assertEquals("No tienes autorización para participar en esta elección.", error.getMessage());
    }

    @Test void rejectsNullSession() {
        assertThrows(VotingException.class,
                () -> fixtures().service.getAvailableElections(null));
    }

    @Test void returnsCandidaciesForAvailableElection() {
        assertEquals(List.of(candidacy), fixtures().service.getCandidacies(
                sessionWith("EMITIR_VOTO"), 1));
    }

    @Test void rejectsMissingOrUnavailableElection() {
        Fixtures missing = fixtures();
        missing.electionDao.byId = Optional.empty();
        assertThrows(VotingException.class, () -> missing.service.getCandidacies(
                sessionWith("EMITIR_VOTO"), 99));

        Fixtures unavailable = fixtures();
        unavailable.electionDao.byId = Optional.of(election(1, false));
        assertThrows(VotingException.class, () -> unavailable.service.getCandidacies(
                sessionWith("EMITIR_VOTO"), 1));
    }

    @Test void rejectsCandidacyFromAnotherElection() {
        Fixtures fixtures = fixtures();
        fixtures.candidacyDao.items = List.of(new Candidacy(
                10, 2, 3, "Persona Candidata", "Propuesta", "Descripción"));

        assertThrows(VotingException.class, () -> fixtures.service.castVote(
                sessionWith("EMITIR_VOTO"), 1, 10));
        assertEquals(0, fixtures.voteDao.calls);
    }

    @Test void emitsVoteExactlyOnce() {
        Fixtures fixtures = fixtures();
        fixtures.service.castVote(sessionWith("EMITIR_VOTO"), 1, 10);
        assertEquals(1, fixtures.voteDao.calls);
    }

    @Test void receiptDoesNotContainVotingIntentOrIdentity() {
        VoteReceipt receipt = fixtures().service.castVote(
                sessionWith("EMITIR_VOTO"), 1, 10);
        assertEquals(1, receipt.electionId());
        assertEquals("codigo-seguro-de-prueba", receipt.verificationCode());
        assertFalse(receipt.toString().contains("codigo-seguro-de-prueba"));
        assertFalse(receipt.toString().contains("Persona Candidata"));
    }

    @Test void translatesDataFailureToSafePublicError() {
        Fixtures fixtures = fixtures();
        fixtures.voteDao.failure = new DataAccessException("detalle interno", new Exception());
        VotingException error = assertThrows(VotingException.class,
                () -> fixtures.service.castVote(sessionWith("EMITIR_VOTO"), 1, 10));
        assertEquals("No fue posible registrar el voto.", error.getMessage());
        assertTrue(error.getCause() instanceof DataAccessException);
    }

    @Test void doesNotCallVoteDaoWhenValidationFails() {
        Fixtures fixtures = fixtures();
        assertThrows(VotingException.class,
                () -> fixtures.service.castVote(sessionWith(), 1, 10));
        assertEquals(0, fixtures.voteDao.calls);
    }

    @Test void roleNameAloneDoesNotAuthorizeVoting() {
        UserSession studentWithoutPermission = sessionWith();
        assertEquals("ESTUDIANTE", studentWithoutPermission.user().roleName());
        assertThrows(VotingException.class,
                () -> fixtures().service.castVote(studentWithoutPermission, 1, 10));
    }

    private Fixtures fixtures() {
        FakeElectionDao electionDao = new FakeElectionDao(activeElection);
        FakeCandidacyDao candidacyDao = new FakeCandidacyDao(List.of(candidacy));
        FakeVoteDao voteDao = new FakeVoteDao();
        return new Fixtures(
                new VotingService(electionDao, candidacyDao, voteDao),
                electionDao, candidacyDao, voteDao);
    }

    private static Election election(int id, boolean available) {
        LocalDateTime now = LocalDateTime.now();
        return new Election(id, "Elección de prueba", "Descripción",
                available ? now.minusHours(1) : now.plusHours(1),
                available ? now.plusHours(1) : now.plusHours(2),
                available ? "ACTIVA" : "PROGRAMADA");
    }

    private static UserSession sessionWith(String... permissions) {
        AuthenticatedUser user = new AuthenticatedUser(
                7, "DOC-TEST", "Nombre", "Apellido", "persona@example.test",
                2, "ESTUDIANTE", Set.of(permissions));
        return new UserSession(user, Instant.now());
    }

    private record Fixtures(
            VotingService service, FakeElectionDao electionDao,
            FakeCandidacyDao candidacyDao, FakeVoteDao voteDao) {}

    private static final class FakeElectionDao implements ElectionDao {
        private List<Election> available;
        private Optional<Election> byId;
        FakeElectionDao(Election election) {
            available = List.of(election);
            byId = Optional.of(election);
        }
        public List<Election> findAvailableElections() { return available; }
        public Optional<Election> findById(int id) { return byId; }
    }

    private static final class FakeCandidacyDao implements CandidacyDao {
        private List<Candidacy> items;
        FakeCandidacyDao(List<Candidacy> items) { this.items = items; }
        public List<Candidacy> findByElectionId(int id) { return items; }
    }

    private static final class FakeVoteDao implements VoteDao {
        private int calls;
        private DataAccessException failure;
        public String registerVote(int userId, int electionId, int candidacyId) {
            calls++;
            if (failure != null) throw failure;
            return "codigo-seguro-de-prueba";
        }
    }
}
