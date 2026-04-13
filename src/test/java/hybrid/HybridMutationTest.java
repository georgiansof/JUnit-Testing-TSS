package hybrid;

import engine.AdvancedRiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pachet 4: Hybrid Mutation - Validări de Graniță și Calcule Exacte")
class HybridMutationTest {
    private AdvancedRiskEngine engine;
    private final List<AdvancedRiskEngine.Claim> emptyClaims = List.of();

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @Test
    @DisplayName("Acceptă vârste la limitele de validare (18 și 99 ani)")
    void testValidAgeBoundaries() {
        assertDoesNotThrow(() -> engine.calculatePremium(18, 0, 0, emptyClaims));
        assertDoesNotThrow(() -> engine.calculatePremium(99, 0, 0, emptyClaims));
    }

    @Test
    @DisplayName("Acceptă experiență la limitele de validare (0 și max logic)")
    void testValidExperienceBoundaries() {
        assertDoesNotThrow(() -> engine.calculatePremium(30, 0, 0, emptyClaims));

        // Experiența maximă logică la 30 ani este 12 (30 ani - 18 ani minim = 12)
        assertDoesNotThrow(() -> engine.calculatePremium(30, 12, 0, emptyClaims));
    }

    @Test
    @DisplayName("Acceptă ani de loialitate la limitele de validare (0 și 10 ani)")
    void testValidLoyaltyBoundaries() {
        assertDoesNotThrow(() -> engine.calculatePremium(35, 5, 0, emptyClaims));
        assertDoesNotThrow(() -> engine.calculatePremium(35, 5, 10, emptyClaims));
    }

    @Test
    @DisplayName("Acceptă daune noi (0 ani vechime)")
    void testValidClaimAgeBoundary() {
        var recentClaim = List.of(new AdvancedRiskEngine.Claim(100, 50, 0));
        assertDoesNotThrow(() -> engine.calculatePremium(30, 5, 0, recentClaim));
    }

    @Test
    @DisplayName("Aplică multiplicator de risc maxim la exact 25 de ani")
    void testHighRiskMultiplierAtExactAgeBoundary() {
        // Multiplicator 1.5x aplicat pentru pragul de 25 ani (bază 1000.0 -> 1500.0)
        assertEquals(1500.0, engine.calculatePremium(25, 0, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Aplică multiplicator de risc maxim la exact 2 ani experiență")
    void testHighRiskMultiplierAtExactExperienceBoundary() {
        // Multiplicator 1.5x aplicat pentru pragul de 2 ani experiență
        assertEquals(1500.0, engine.calculatePremium(22, 2, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Aplică preț standard la exact 65 de ani (înainte de pragul senior)")
    void testStandardPremiumBeforeSeniorAgeBoundary() {
        // La exact 65 ani prețul rămâne standard (1000.0). Multiplicatorul se aplică abia > 65.
        assertEquals(1000.0, engine.calculatePremium(65, 30, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Anulează discountul de fidelitate pentru daună de exact 2 ani")
    void testNoLoyaltyDiscountForClaimExactlyTwoYearsOld() {
        // Dauna trebuie să fie STRICT mai veche de 2 ani (> 2) pentru a nu bloca discountul
        var claimAtThreshold = List.of(new AdvancedRiskEngine.Claim(0, 0, 2));
        assertEquals(1000.0, engine.calculatePremium(35, 10, 1, claimAtThreshold), 0.01);
    }

    @Test
    @DisplayName("Calculează penalizarea cumulată exactă pentru daune multiple")
    void testExactPenaltyCalculationForMultipleClaims() {
        // Preț de bază: 1000.0
        // Penalizare per daună: 8.0 lei. Total pentru 2 daune: 1000.0 + 16.0 = 1016.0
        var claims = List.of(
                new AdvancedRiskEngine.Claim(1000, 100, 1),
                new AdvancedRiskEngine.Claim(1000, 100, 1)
        );
        assertEquals(1016.0, engine.calculatePremium(30, 10, 0, claims), 0.01);
    }

    @Test
    @DisplayName("Aplică discountul de fidelitate exact la pragul maxim de plafonare (25%)")
    void testLoyaltyDiscountExactlyAtCappingThreshold() {
        // 5 ani de loialitate generează fix pragul maxim de discount (5 * 0.05 = 0.25 -> 750.0 lei)
        assertEquals(750.0, engine.calculatePremium(40, 10, 5, emptyClaims), 0.01);
    }
}