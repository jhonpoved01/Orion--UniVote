package io.github.jhonpoved01.univote.service;

import io.github.jhonpoved01.univote.dao.VoteVerificationDao;
import io.github.jhonpoved01.univote.exception.DataAccessException;
import io.github.jhonpoved01.univote.exception.VoteVerificationException;
import io.github.jhonpoved01.univote.model.VoteVerification;
import io.github.jhonpoved01.univote.security.UserSession;
import java.util.Objects;

public final class VoteVerificationService {
    private static final int CODE_LENGTH = 64;
    private final VoteVerificationDao verificationDao;

    public VoteVerificationService(VoteVerificationDao verificationDao) {
        this.verificationDao = Objects.requireNonNull(verificationDao);
    }

    public VoteVerification verify(UserSession session, String verificationCode) {
        if (session == null) {
            throw new VoteVerificationException("Debes iniciar sesión para verificar un comprobante.");
        }
        String normalized = normalize(verificationCode);
        try {
            return verificationDao.verify(normalized)
                    .orElseThrow(() -> new VoteVerificationException(
                            "No se encontró un voto registrado con este código."));
        } catch (DataAccessException exception) {
            throw new VoteVerificationException(
                    "No fue posible verificar el comprobante.", exception);
        }
    }

    private static String normalize(String code) {
        if (code == null) {
            throw invalidFormat();
        }
        String normalized = code.strip();
        if (normalized.length() != CODE_LENGTH || !normalized.matches("[0-9a-fA-F]{64}")) {
            throw invalidFormat();
        }
        return normalized;
    }

    private static VoteVerificationException invalidFormat() {
        return new VoteVerificationException(
                "El código de verificación no tiene un formato válido.");
    }
}
