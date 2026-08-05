package io.github.jhonpoved01.univote.service;

import io.github.jhonpoved01.univote.dao.ElectionDao;
import io.github.jhonpoved01.univote.dao.ElectionResultDao;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.exception.ElectionResultsException;
import io.github.jhonpoved01.univote.model.Election;
import io.github.jhonpoved01.univote.model.ElectionResult;
import io.github.jhonpoved01.univote.security.UserSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class ElectionResultsService {
    private static final String RESULTS_PERMISSION = "CONSULTAR_RESULTADOS";
    private final ElectionDao electionDao;
    private final ElectionResultDao resultDao;

    public ElectionResultsService(ElectionDao electionDao, ElectionResultDao resultDao) {
        this.electionDao = Objects.requireNonNull(electionDao);
        this.resultDao = Objects.requireNonNull(resultDao);
    }

    public List<Election> getPublishableElections(UserSession session) {
        authorize(session);
        try {
            return electionDao.findFinishedElections();
        } catch (DataAccessException exception) {
            throw technicalFailure(exception);
        }
    }

    public List<ElectionResult> getPublishedResults(UserSession session, int electionId) {
        authorize(session);
        try {
            Election election = electionDao.findById(electionId)
                    .orElseThrow(ElectionResultsService::notAvailable);
            LocalDateTime now = LocalDateTime.now();
            if (!"FINALIZADA".equals(election.status()) || now.isBefore(election.endsAt())) {
                throw notAvailable();
            }
            return resultDao.findByElectionId(electionId);
        } catch (DataAccessException exception) {
            throw technicalFailure(exception);
        }
    }

    private static void authorize(UserSession session) {
        if (session == null || !session.hasPermission(RESULTS_PERMISSION)) {
            throw new ElectionResultsException("No tienes autorización para consultar resultados.");
        }
    }

    private static ElectionResultsException notAvailable() {
        return new ElectionResultsException(
                "Los resultados de esta elección todavía no están disponibles.");
    }

    private static ElectionResultsException technicalFailure(Throwable cause) {
        return new ElectionResultsException("No fue posible consultar los resultados.", cause);
    }
}
