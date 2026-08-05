package io.github.jhonpoved01.univote.exception;

public final class DataAccessException extends RuntimeException {

    public DataAccessException(String safeMessage, Throwable cause) {
        super(safeMessage, cause);
    }
}
