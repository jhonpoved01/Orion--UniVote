package io.github.jhonpoved01.univote.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhonpoved01.univote.config.DatabaseConfig;
import io.github.jhonpoved01.univote.config.DatabaseConfigLoader;
import io.github.jhonpoved01.univote.dao.JdbcElectionDao;
import io.github.jhonpoved01.univote.dao.JdbcElectionResultDao;
import io.github.jhonpoved01.univote.dao.JdbcVoteVerificationDao;
import io.github.jhonpoved01.univote.model.Election;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "univote.integration.database", matches = "true")
class VerificationResultsIntegrationTest {
    private final DatabaseConnectionFactory connectionFactory = connectionFactory();

    @Test void syntheticUnknownCodeIsNotFoundWithoutWriting() {
        assertTrue(new JdbcVoteVerificationDao(connectionFactory)
                .verify("f".repeat(64)).isEmpty());
    }

    @Test void readsStoredProcedureResultsOnlyWhenFinishedElectionExists() {
        List<Election> elections = new JdbcElectionDao(connectionFactory).findFinishedElections();
        Assumptions.assumeFalse(elections.isEmpty(), "No existe una elección finalizada publicable");
        new JdbcElectionResultDao(connectionFactory).findByElectionId(elections.getFirst().id());
    }

    private static DatabaseConnectionFactory connectionFactory() {
        DatabaseConfig config = new DatabaseConfigLoader().load();
        return new DatabaseConnectionFactory(config);
    }
}
