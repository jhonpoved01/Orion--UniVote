package io.github.jhonpoved01.univote.exception;

public final class VoteVerificationException extends RuntimeException {
    public VoteVerificationException(String message) { super(message); }
    public VoteVerificationException(String message, Throwable cause) { super(message, cause); }
}
