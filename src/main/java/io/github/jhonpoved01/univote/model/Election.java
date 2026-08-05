package io.github.jhonpoved01.univote.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record Election(
        int id,
        String name,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String status) {

    public Election {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador de elección debe ser positivo");
        }
        name = requireText(name, "name");
        description = description == null ? "" : description;
        Objects.requireNonNull(startsAt, "startsAt no puede ser null");
        Objects.requireNonNull(endsAt, "endsAt no puede ser null");
        status = requireText(status, "status");
        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("El cierre debe ser posterior al inicio");
        }
    }

    public boolean isAvailableAt(LocalDateTime moment) {
        Objects.requireNonNull(moment, "moment no puede ser null");
        return "ACTIVA".equals(status)
                && !moment.isBefore(startsAt)
                && !moment.isAfter(endsAt);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " no puede estar vacío");
        }
        return value;
    }
}
