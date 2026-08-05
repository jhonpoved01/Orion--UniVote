package io.github.jhonpoved01.univote.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.jupiter.api.Test;

class BCryptPasswordVerifierTest {

    private final PasswordVerifier verifier = new BCryptPasswordVerifier();

    @Test
    void acceptsCorrectPassword() {
        char[] password = "ContraseñaDePrueba!".toCharArray();
        String hash = BCrypt.withDefaults().hashToString(4, password);

        assertTrue(verifier.verify(password, hash));
    }

    @Test
    void rejectsIncorrectPassword() {
        String hash = BCrypt.withDefaults().hashToString(4, "CorrectaDePrueba!".toCharArray());

        assertFalse(verifier.verify("IncorrectaDePrueba!".toCharArray(), hash));
    }

    @Test
    void rejectsInvalidHash() {
        assertFalse(verifier.verify("ContraseñaDePrueba!".toCharArray(), "hash-inválido"));
    }

    @Test
    void rejectsNullAndEmptyPassword() {
        String hash = BCrypt.withDefaults().hashToString(4, "ContraseñaDePrueba!".toCharArray());

        assertFalse(verifier.verify(null, hash));
        assertFalse(verifier.verify(new char[0], hash));
    }

    @Test
    void supportsBcrypt2aHashFamilyUsedByDeliveredSchema() {
        char[] password = "CompatibilidadDePrueba!".toCharArray();
        String hash = BCrypt.with(BCrypt.Version.VERSION_2A).hashToString(4, password);

        assertTrue(hash.startsWith("$2a$"));
        assertTrue(verifier.verify(password, hash));
    }
}
