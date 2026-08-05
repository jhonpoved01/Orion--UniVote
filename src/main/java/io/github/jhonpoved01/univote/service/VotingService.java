package io.github.jhonpoved01.univote.service;

import io.github.jhonpoved01.univote.dao.CandidacyDao;
import io.github.jhonpoved01.univote.dao.ElectionDao;
import io.github.jhonpoved01.univote.dao.VoteDao;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.exception.VotingException;
import io.github.jhonpoved01.univote.model.Candidacy;
import io.github.jhonpoved01.univote.model.Election;
import io.github.jhonpoved01.univote.model.VoteReceipt;
import io.github.jhonpoved01.univote.security.UserSession;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class VotingService {
    private static final String VOTE_PERMISSION = "EMITIR_VOTO";
    private final ElectionDao electionDao;
    private final CandidacyDao candidacyDao;
    private final VoteDao voteDao;

    public VotingService(ElectionDao electionDao, CandidacyDao candidacyDao, VoteDao voteDao) {
        this.electionDao = Objects.requireNonNull(electionDao);
        this.candidacyDao = Objects.requireNonNull(candidacyDao);
        this.voteDao = Objects.requireNonNull(voteDao);
    }

    public List<Election> getAvailableElections(UserSession session) {
        authorize(session);
        try {
            return electionDao.findAvailableElections();
        } catch (DataAccessException exception) {
            throw new VotingException("No fue posible cargar la información electoral.", exception);
        }
    }

    public List<Candidacy> getCandidacies(UserSession session, int electionId) {
        authorize(session);
        requireAvailableElection(electionId);
        try {
            return candidacyDao.findByElectionId(electionId);
        } catch (DataAccessException exception) {
            throw new VotingException("No fue posible cargar la información electoral.", exception);
        }
    }

    public VoteReceipt castVote(UserSession session, int electionId, int candidacyId) {
        authorize(session);
        requireAvailableElection(electionId);
        try {
            boolean validCandidacy = candidacyDao.findByElectionId(electionId).stream()
                    .anyMatch(item -> item.id() == candidacyId && item.electionId() == electionId);
            if (!validCandidacy) {
                throw new VotingException("La candidatura seleccionada no es válida.");
            }
            String code = voteDao.registerVote(session.user().userId(), electionId, candidacyId);
            return new VoteReceipt(electionId, code, Instant.now());
        } catch (DataAccessException exception) {
            throw new VotingException("No fue posible registrar el voto.", exception);
        }
    }

    private Election requireAvailableElection(int electionId) {
        try {
            Election election = electionDao.findById(electionId)
                    .orElseThrow(() -> new VotingException(
                            "La elección seleccionada no está disponible."));
            if (!election.isAvailableAt(LocalDateTime.now())) {
                throw new VotingException("La elección seleccionada no está disponible.");
            }
            return election;
        } catch (DataAccessException exception) {
            throw new VotingException("No fue posible cargar la información electoral.", exception);
        }
    }

    private static void authorize(UserSession session) {
        if (session == null || !session.hasPermission(VOTE_PERMISSION)) {
            throw new VotingException("No tienes autorización para participar en esta elección.");
        }
    }
}
