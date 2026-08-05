package io.github.jhonpoved01.univote.security;

import io.github.jhonpoved01.univote.model.AuthenticatedUser;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public record UserSession(AuthenticatedUser user, Instant authenticatedAt) {

    public UserSession {
        Objects.requireNonNull(user, "user no puede ser null");
        Objects.requireNonNull(authenticatedAt, "authenticatedAt no puede ser null");
    }

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }
        String normalized = permission.strip().toUpperCase(Locale.ROOT);
        return user.permissions().stream()
                .map(value -> value.strip().toUpperCase(Locale.ROOT))
                .anyMatch(normalized::equals);
    }
}
