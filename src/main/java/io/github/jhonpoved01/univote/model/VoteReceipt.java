package io.github.jhonpoved01.univote.model;

import java.time.Instant;
import java.util.Objects;

public record VoteReceipt(int electionId, String verificationCode, Instant issuedAt) {

    public VoteReceipt {
        if (electionId <= 0) {
            throw new IllegalArgumentException("El identificador de elección debe ser positivo");
        }
        if (verificationCode == null || verificationCode.isBlank()) {
            throw new IllegalArgumentException("El código de verificación es obligatorio");
        }
        Objects.requireNonNull(issuedAt, "issuedAt no puede ser null");
    }

    @Override
    public String toString() {
        return "VoteReceipt[electionId=" + electionId
                + ", verificationCode=<redacted>, issuedAt=" + issuedAt + ']';
    }
}
