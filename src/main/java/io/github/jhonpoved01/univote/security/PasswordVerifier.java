package io.github.jhonpoved01.univote.security;

public interface PasswordVerifier {

    boolean verify(char[] password, String passwordHash);
}
