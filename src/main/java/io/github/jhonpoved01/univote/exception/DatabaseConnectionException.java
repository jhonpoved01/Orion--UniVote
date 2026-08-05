package io.github.jhonpoved01.univote.exception;

public final class DatabaseConnectionException extends RuntimeException {

    public DatabaseConnectionException(String safeMessage, Throwable cause) {
        super(safeMessage, cause);
    }
}
