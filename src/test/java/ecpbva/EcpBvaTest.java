package ecpbva;

import engine.AdvancedRiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pachet 1: ECP & BVA - Validări și Calcule de Bază")
class EcpBvaTest {
    private AdvancedRiskEngine engine;
    private final List<AdvancedRiskEngine.Claim> emptyClaims = List.of();

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @Test
    @DisplayName("Respinge parametri șofer în afara limitelor valide (Vârstă, Exp, Fidelitate)")
    void testThrowsExceptionForInvalidDriverParameters() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(17, 0, 0, emptyClaims));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(100, 0, 0, emptyClaims));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, -1, 0, emptyClaims));
        // Experiența maximă la 30 de ani este 12 (30 ani - 18 ani vârstă minimă = 12)
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 13, 0, emptyClaims));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, -1, emptyClaims));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 11, emptyClaims));
    }

    @Test
    @DisplayName("Respinge istoric de daune null sau cu atribute invalide")
    void testThrowsExceptionForInvalidClaimData() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0, null));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0, List.of(new AdvancedRiskEngine.Claim(100, -1, 1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0, List.of(new AdvancedRiskEngine.Claim(100, 101, 1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0, List.of(new AdvancedRiskEngine.Claim(100, 50, -1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0, List.of(new AdvancedRiskEngine.Claim(-1, 50, 1))));
    }

    @Test
    @DisplayName("Aplică corect multiplicatorii de risc pentru profilul șoferului")
    void testCalculatesPremiumBasedOnDriverRiskProfile() {
        // Multiplicator 1.5x (Tânăr Începător)
        assertEquals(1500.0, engine.calculatePremium(20, 1, 0, emptyClaims), 0.01);

        // Preț Bază 1000.0 (Tânăr, dar experiență > 2 ani anulează riscul)
        assertEquals(1000.0, engine.calculatePremium(25, 3, 0, emptyClaims), 0.01);

        // Multiplicator 1.2x (Senior)
        assertEquals(1200.0, engine.calculatePremium(70, 30, 0, emptyClaims), 0.01);

        // Preț Bază 1000.0 (Adult Standard, fără multiplicatori)
        assertEquals(1000.0, engine.calculatePremium(35, 10, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Aplică multiplicator de risc maxim pentru istoric încărcat (>= 3 daune)")
    void testAppliesHighRiskMultiplierForFrequentClaims() {
        var frequentClaims = List.of(
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1)
        );
        assertEquals(1500.0, engine.calculatePremium(35, 10, 0, frequentClaims), 0.01);
    }

    @Test
    @DisplayName("Evaluează corect discountul de fidelitate și plafonarea maximă")
    void testCalculatesLoyaltyDiscountBasedOnClaimHistory() {
        // Fără daune: Discount complet de 0.05
        assertEquals(950.0, engine.calculatePremium(35, 10, 1, emptyClaims), 0.01);

        // O singură daună veche (> 2 ani): Discount redus la 0.02
        var oldSingleClaim = List.of(new AdvancedRiskEngine.Claim(0, 0, 3));
        assertEquals(980.0, engine.calculatePremium(35, 10, 1, oldSingleClaim), 0.01);

        // O singură daună recentă (<= 2 ani): Anulează discountul pe acel an
        var recentSingleClaim = List.of(new AdvancedRiskEngine.Claim(0, 0, 1));
        assertEquals(1000.0, engine.calculatePremium(35, 10, 1, recentSingleClaim), 0.01);

        // Daune multiple: Anulează complet discountul
        var multipleClaims = List.of(
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1)
        );
        assertEquals(1000.0, engine.calculatePremium(35, 10, 1, multipleClaims), 0.01);

        // Plafonare: Maxim 25% reducere, atins la 5+ ani
        assertEquals(750.0, engine.calculatePremium(40, 10, 6, emptyClaims), 0.01);
    }
}