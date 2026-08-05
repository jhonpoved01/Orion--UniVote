package io.github.jhonpoved01.univote.exception;

public final class AuthenticationException extends RuntimeException {

    public AuthenticationException(String safeMessage) {
        super(safeMessage);
    }

    public AuthenticationException(String safeMessage, Throwable cause) {
        super(safeMessage, cause);
    }
}
