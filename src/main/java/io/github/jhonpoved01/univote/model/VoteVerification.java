package io.github.jhonpoved01.univote.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record VoteVerification(boolean registered, LocalDateTime registeredAt) {
    public VoteVerification {
        if (!registered) {
            throw new IllegalArgumentException("La verificación debe representar un voto registrado");
        }
        Objects.requireNonNull(registeredAt, "registeredAt no puede ser null");
    }
}
