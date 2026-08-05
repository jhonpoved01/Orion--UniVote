package io.github.jhonpoved01.univote;

import io.github.jhonpoved01.univote.config.DatabaseConfig;
import io.github.jhonpoved01.univote.config.DatabaseConfigLoader;
import io.github.jhonpoved01.univote.dao.JdbcUserDao;
import io.github.jhonpoved01.univote.dao.JdbcElectionDao;
import io.github.jhonpoved01.univote.dao.JdbcCandidacyDao;
import io.github.jhonpoved01.univote.dao.JdbcVoteDao;
import io.github.jhonpoved01.univote.dao.JdbcVoteVerificationDao;
import io.github.jhonpoved01.univote.dao.JdbcElectionResultDao;
import io.github.jhonpoved01.univote.dao.UserDao;
import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.security.BCryptPasswordVerifier;
import io.github.jhonpoved01.univote.security.PasswordVerifier;
import io.github.jhonpoved01.univote.service.AuthenticationService;
import io.github.jhonpoved01.univote.service.VotingService;
import io.github.jhonpoved01.univote.service.VoteVerificationService;
import io.github.jhonpoved01.univote.service.ElectionResultsService;

public record ApplicationServices(
        AuthenticationService authenticationService,
        VotingService votingService,
        VoteVerificationService voteVerificationService,
        ElectionResultsService electionResultsService) {

    public static ApplicationServices create() {
        DatabaseConfig databaseConfig = new DatabaseConfigLoader().load();
        DatabaseConnectionFactory connectionFactory =
                new DatabaseConnectionFactory(databaseConfig);
        UserDao userDao = new JdbcUserDao(connectionFactory);
        PasswordVerifier passwordVerifier = new BCryptPasswordVerifier();
        AuthenticationService authenticationService =
                new AuthenticationService(userDao, passwordVerifier);
        JdbcElectionDao electionDao = new JdbcElectionDao(connectionFactory);
        VotingService votingService = new VotingService(
                electionDao,
                new JdbcCandidacyDao(connectionFactory),
                new JdbcVoteDao(connectionFactory));
        VoteVerificationService verificationService = new VoteVerificationService(
                new JdbcVoteVerificationDao(connectionFactory));
        ElectionResultsService resultsService = new ElectionResultsService(
                electionDao, new JdbcElectionResultDao(connectionFactory));
        return new ApplicationServices(
                authenticationService, votingService, verificationService, resultsService);
    }
}
