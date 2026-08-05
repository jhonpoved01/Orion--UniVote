package io.github.jhonpoved01.univote.database;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.jhonpoved01.univote.config.DatabaseConfig;
import io.github.jhonpoved01.univote.config.DatabaseConfigLoader;
import io.github.jhonpoved01.univote.dao.JdbcCandidacyDao;
import io.github.jhonpoved01.univote.dao.JdbcElectionDao;
import io.github.jhonpoved01.univote.model.Election;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "univote.integration.database", matches = "true")
class ElectoralReadIntegrationTest {
    @Test void readsAvailableElectionAndItsCandidaciesWithoutWriting() {
        DatabaseConfig config = new DatabaseConfigLoader().load();
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(config);
        JdbcElectionDao electionDao = new JdbcElectionDao(connectionFactory);
        JdbcCandidacyDao candidacyDao = new JdbcCandidacyDao(connectionFactory);

        List<Election> elections = electionDao.findAvailableElections();

        assertFalse(elections.isEmpty());
        assertFalse(candidacyDao.findByElectionId(elections.getFirst().id()).isEmpty());
    }
}
