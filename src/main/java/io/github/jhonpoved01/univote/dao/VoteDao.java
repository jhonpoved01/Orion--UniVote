package io.github.jhonpoved01.univote.dao;

public interface VoteDao {
    String registerVote(int userId, int electionId, int candidacyId);
}
