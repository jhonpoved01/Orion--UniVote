package io.github.jhonpoved01.univote.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class BCryptPasswordVerifier implements PasswordVerifier {

    @Override
    public boolean verify(char[] password, String passwordHash) {
        if (password == null || password.length == 0
                || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.verifyer().verify(password, passwordHash).verified;
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException exception) {
            return false;
        }
    }
}
