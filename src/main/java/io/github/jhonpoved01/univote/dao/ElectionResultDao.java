package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.model.ElectionResult;
import java.util.List;

public interface ElectionResultDao {
    List<ElectionResult> findByElectionId(int electionId);
}
