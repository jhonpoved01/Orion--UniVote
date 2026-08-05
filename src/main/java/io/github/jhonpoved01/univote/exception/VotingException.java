package io.github.jhonpoved01.univote.exception;

public final class VotingException extends RuntimeException {
    public VotingException(String safeMessage) { super(safeMessage); }
    public VotingException(String safeMessage, Throwable cause) { super(safeMessage, cause); }
}
