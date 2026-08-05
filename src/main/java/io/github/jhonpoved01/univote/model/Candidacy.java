package io.github.jhonpoved01.univote.model;

public record Candidacy(
        int id,
        int electionId,
        int listNumber,
        String candidateName,
        String proposalTitle,
        String proposal) {

    public Candidacy {
        if (id <= 0 || electionId <= 0 || listNumber <= 0) {
            throw new IllegalArgumentException("Los identificadores y el número de lista deben ser positivos");
        }
        candidateName = requireText(candidateName, "candidateName");
        proposalTitle = requireText(proposalTitle, "proposalTitle");
        proposal = requireText(proposal, "proposal");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " no puede estar vacío");
        }
        return value;
    }
}
