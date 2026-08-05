package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.model.Election;
import java.util.List;
import java.util.Optional;

public interface ElectionDao {
    List<Election> findAvailableElections();
    List<Election> findFinishedElections();
    Optional<Election> findById(int electionId);
}
