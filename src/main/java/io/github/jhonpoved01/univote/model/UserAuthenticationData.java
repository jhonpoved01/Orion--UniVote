package io.github.jhonpoved01.univote.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record UserAuthenticationData(
        int userId,
        String document,
        String firstNames,
        String lastNames,
        String email,
        String passwordHash,
        boolean active,
        int roleId,
        String roleName,
        Set<String> permissions) {

    public UserAuthenticationData {
        Objects.requireNonNull(passwordHash, "passwordHash no puede ser null");
        Objects.requireNonNull(permissions, "permissions no puede ser null");
        permissions = Set.copyOf(new LinkedHashSet<>(permissions));
    }

    public AuthenticatedUser toAuthenticatedUser() {
        return new AuthenticatedUser(
                userId, document, firstNames, lastNames, email, roleId, roleName, permissions);
    }

    @Override
    public String toString() {
        return "UserAuthenticationData[userId=" + userId
                + ", document=" + document
                + ", firstNames=" + firstNames
                + ", lastNames=" + lastNames
                + ", email=" + email
                + ", passwordHash=<redacted>"
                + ", active=" + active
                + ", roleId=" + roleId
                + ", roleName=" + roleName
                + ", permissions=" + permissions + ']';
    }
}
