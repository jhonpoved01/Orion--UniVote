package io.github.jhonpoved01.univote.model;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ResultsModelsTest {
    @Test void validatesCountsAndPercentageRange() {
        assertThrows(IllegalArgumentException.class, () -> result(-1, new BigDecimal("1")));
        assertThrows(IllegalArgumentException.class, () -> result(1, new BigDecimal("-0.01")));
        assertThrows(IllegalArgumentException.class, () -> result(1, new BigDecimal("100.01")));
    }
    @Test void usesBigDecimalAndContainsNoVoterData() {
        ElectionResult result = result(2, new BigDecimal("33.33"));
        assertEquals(BigDecimal.class, result.percentage().getClass());
        String text = result.toString().toLowerCase();
        assertFalse(text.contains("documento")); assertFalse(text.contains("correo"));
    }
    @Test void verificationRequiresSafeTimestamp() {
        assertThrows(NullPointerException.class, () -> new VoteVerification(true, null));
        assertNotNull(new VoteVerification(true, LocalDateTime.now()).registeredAt());
    }
    private static ElectionResult result(long votes, BigDecimal percentage) {
        return new ElectionResult(1, "Elección", "FINALIZADA", 2, 1,
                "Persona", "Propuesta", votes, percentage);
    }
}
