package io.github.jhonpoved01.univote.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ElectoralModelsTest {
    @Test void validatesPositiveIdentifiers() {
        LocalDateTime now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class,
                () -> new Election(0, "Elección", "", now, now.plusHours(1), "ACTIVA"));
        assertThrows(IllegalArgumentException.class,
                () -> new Candidacy(0, 1, 1, "Nombre", "Propuesta", "Descripción"));
        assertThrows(IllegalArgumentException.class,
                () -> new VoteReceipt(0, "codigo", Instant.now()));
    }

    @Test void electionAvailabilityUsesStateAndPeriod() {
        LocalDateTime now = LocalDateTime.now();
        Election election = new Election(
                1, "Elección", "", now.minusMinutes(1), now.plusMinutes(1), "ACTIVA");
        assertTrue(election.isAvailableAt(now));
        assertFalse(election.isAvailableAt(now.plusHours(1)));
    }

    @Test void receiptStructureContainsNoIdentityOrCandidacy() {
        Set<String> fields = Arrays.stream(VoteReceipt.class.getRecordComponents())
                .map(RecordComponent::getName).collect(Collectors.toSet());
        assertFalse(fields.contains("userId"));
        assertFalse(fields.contains("candidacyId"));
        assertFalse(fields.contains("document"));
        assertFalse(fields.contains("email"));
        assertFalse(fields.contains("candidateName"));
    }

    @Test void receiptStringRedactsVerificationCode() {
        VoteReceipt receipt = new VoteReceipt(1, "codigo-no-publicable", Instant.now());
        assertFalse(receipt.toString().contains("codigo-no-publicable"));
    }
}
