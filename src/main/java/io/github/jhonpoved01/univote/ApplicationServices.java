package io.github.jhonpoved01.univote;

import io.github.jhonpoved01.univote.config.DatabaseConfig;
import io.github.jhonpoved01.univote.config.DatabaseConfigLoader;
import io.github.jhonpoved01.univote.dao.JdbcUserDao;
import io.github.jhonpoved01.univote.dao.UserDao;
import io.github.jhonpoved01.univote.database.DatabaseConnectionFactory;
import io.github.jhonpoved01.univote.security.BCryptPasswordVerifier;
import io.github.jhonpoved01.univote.security.PasswordVerifier;
import io.github.jhonpoved01.univote.service.AuthenticationService;

public record ApplicationServices(AuthenticationService authenticationService) {

    public static ApplicationServices create() {
        DatabaseConfig databaseConfig = new DatabaseConfigLoader().load();
        DatabaseConnectionFactory connectionFactory =
                new DatabaseConnectionFactory(databaseConfig);
        UserDao userDao = new JdbcUserDao(connectionFactory);
        PasswordVerifier passwordVerifier = new BCryptPasswordVerifier();
        return new ApplicationServices(new AuthenticationService(userDao, passwordVerifier));
    }
}
