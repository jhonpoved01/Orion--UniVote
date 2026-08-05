package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.model.Candidacy;
import java.util.List;

public interface CandidacyDao {
    List<Candidacy> findByElectionId(int electionId);
}
