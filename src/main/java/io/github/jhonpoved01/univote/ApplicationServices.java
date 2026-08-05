package io.github.jhonpoved01.univote;

import io.github.jhonpoved01.univote.config.DatabaseConfig;
import io.github.jhonpoved01.univote.config.DatabaseConfigLoader;
import io.github.jhonpoved01.univote.dao.JdbcUserDao;
import io.github.jhonpoved01.univote.dao.JdbcElectionDao;
import io.github.jhonpoved01.univote.dao.JdbcCandidacyDao;
import io.github.jhonpoved01.univote.dao.JdbcVoteDao;
import io.github.jhonpoved01.univote.dao.UserDao;
import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.security.BCryptPasswordVerifier;
import io.github.jhonpoved01.univote.security.PasswordVerifier;
import io.github.jhonpoved01.univote.service.AuthenticationService;
import io.github.jhonpoved01.univote.service.VotingService;

public record ApplicationServices(
        AuthenticationService authenticationService, VotingService votingService) {

    public static ApplicationServices create() {
        DatabaseConfig databaseConfig = new DatabaseConfigLoader().load();
        DatabaseConnectionFactory connectionFactory =
                new DatabaseConnectionFactory(databaseConfig);
        UserDao userDao = new JdbcUserDao(connectionFactory);
        PasswordVerifier passwordVerifier = new BCryptPasswordVerifier();
        AuthenticationService authenticationService =
                new AuthenticationService(userDao, passwordVerifier);
        VotingService votingService = new VotingService(
                new JdbcElectionDao(connectionFactory),
                new JdbcCandidacyDao(connectionFactory),
                new JdbcVoteDao(connectionFactory));
        return new ApplicationServices(authenticationService, votingService);
    }
}
