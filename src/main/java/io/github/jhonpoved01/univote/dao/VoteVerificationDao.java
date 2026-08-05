package io.github.jhonpoved01.univote.dao;

import io.github.jhonpoved01.univote.model.VoteVerification;
import java.util.Optional;

public interface VoteVerificationDao {
    Optional<VoteVerification> verify(String verificationCode);
}
