package io.github.jhonpoved01.univote.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record AuthenticatedUser(
        int userId,
        String document,
        String firstNames,
        String lastNames,
        String email,
        int roleId,
        String roleName,
        Set<String> permissions) {

    public AuthenticatedUser {
        if (userId <= 0 || roleId <= 0) {
            throw new IllegalArgumentException("Los identificadores deben ser positivos");
        }
        document = requireText(document, "document");
        firstNames = requireText(firstNames, "firstNames");
        lastNames = requireText(lastNames, "lastNames");
        email = requireText(email, "email");
        roleName = requireText(roleName, "roleName");
        Objects.requireNonNull(permissions, "permissions no puede ser null");
        permissions = Set.copyOf(new LinkedHashSet<>(permissions));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " no puede estar vacío");
        }
        return value;
    }
}
