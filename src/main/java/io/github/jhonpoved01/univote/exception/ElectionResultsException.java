package io.github.jhonpoved01.univote.exception;

public final class ElectionResultsException extends RuntimeException {
    public ElectionResultsException(String message) { super(message); }
    public ElectionResultsException(String message, Throwable cause) { super(message, cause); }
}
