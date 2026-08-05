package io.github.jhonpoved01.univote.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jhonpoved01.univote.model.AuthenticatedUser;
import io.github.jhonpoved01.univote.model.UserAuthenticationData;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserSessionTest {

    @Test
    void authenticatedUserDoesNotExposePasswordHash() {
        Set<String> componentNames = Arrays.stream(AuthenticatedUser.class.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(java.util.stream.Collectors.toSet());

        assertFalse(componentNames.contains("password"));
        assertFalse(componentNames.contains("passwordHash"));
    }

    @Test
    void checksPermissionsWithConsistentCaseAndWhitespace() {
        UserSession session = new UserSession(user(Set.of("EMITIR_VOTO")), Instant.now());

        assertTrue(session.hasPermission(" emitir_voto "));
        assertFalse(session.hasPermission("GESTIONAR_USUARIOS"));
        assertFalse(session.hasPermission(null));
    }

    @Test
    void permissionsCannotBeModifiedExternally() {
        Set<String> source = new LinkedHashSet<>(Set.of("EMITIR_VOTO"));
        AuthenticatedUser user = user(source);
        source.add("GESTIONAR_USUARIOS");

        assertFalse(user.permissions().contains("GESTIONAR_USUARIOS"));
        assertThrows(UnsupportedOperationException.class,
                () -> user.permissions().add("GESTIONAR_USUARIOS"));
    }

    @Test
    void authenticationDataStringRedactsHash() {
        UserAuthenticationData data = new UserAuthenticationData(
                1, "DOC-1", "Nombre", "Apellido", "persona@example.test",
                "hash-secreto-de-prueba", true, 2, "ESTUDIANTE", Set.of("EMITIR_VOTO"));

        assertFalse(data.toString().contains("hash-secreto-de-prueba"));
        assertTrue(data.toString().contains("passwordHash=<redacted>"));
        assertFalse(user(Set.of()).toString().toLowerCase().contains("password"));
    }

    private static AuthenticatedUser user(Set<String> permissions) {
        return new AuthenticatedUser(
                1, "DOC-1", "Nombre", "Apellido", "persona@example.test",
                2, "ESTUDIANTE", permissions);
    }
}
