package io.github.jhonpoved01.univote.exception;

public final class ViewNavigationException extends RuntimeException {

    public ViewNavigationException(String safeMessage) {
        super(safeMessage);
    }

    public ViewNavigationException(String safeMessage, Throwable cause) {
        super(safeMessage, cause);
    }
}
