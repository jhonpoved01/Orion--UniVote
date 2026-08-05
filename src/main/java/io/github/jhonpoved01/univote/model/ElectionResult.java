package io.github.jhonpoved01.univote.model;

import java.math.BigDecimal;
import java.util.Objects;

public record ElectionResult(
        int electionId, String electionName, String electionStatus,
        int candidacyId, int listNumber, String candidateName,
        String proposalTitle, long totalVotes, BigDecimal percentage) {
    public ElectionResult {
        if (electionId <= 0 || candidacyId <= 0 || listNumber <= 0) {
            throw new IllegalArgumentException("Los identificadores deben ser positivos");
        }
        if (totalVotes < 0) {
            throw new IllegalArgumentException("El total de votos no puede ser negativo");
        }
        electionName = requireText(electionName);
        electionStatus = requireText(electionStatus);
        candidateName = requireText(candidateName);
        proposalTitle = requireText(proposalTitle);
        Objects.requireNonNull(percentage, "percentage no puede ser null");
        if (percentage.compareTo(BigDecimal.ZERO) < 0
                || percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El texto es obligatorio");
        }
        return value;
    }
}
