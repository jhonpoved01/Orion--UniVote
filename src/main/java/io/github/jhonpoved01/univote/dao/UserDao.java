package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.model.UserAuthenticationData;
import java.util.Optional;

public interface UserDao {

    Optional<UserAuthenticationData> findForAuthentication(String identifier);
}
