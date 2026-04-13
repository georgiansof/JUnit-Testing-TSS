package causeeffect;

import engine.AdvancedRiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pachet 3: Analiză Cauză-Efect - Reguli de Business și Decizii")
class CauseEffectTest {
    private AdvancedRiskEngine engine;
    private final List<AdvancedRiskEngine.Claim> emptyClaims = List.of();

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @Test
    @DisplayName("Respinge date de intrare invalide pentru profilul șoferului și daune")
    void testThrowsExceptionForInvalidInputCauses() {
        // Validări frontiere vârstă
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(17, 0, 0, emptyClaims));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(100, 0, 0, emptyClaims));

        // Validări frontiere experiență (inclusiv maximul logic admis: vârstă - 18)
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, -1, 0, emptyClaims));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 13, 0, emptyClaims));

        // Validări frontiere loialitate (maxim 10 ani)
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, -1, emptyClaims));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 11, emptyClaims));

        // Validări corupere date daune
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0, null));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(100, -1, 1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(100, 101, 1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(100, 50, -1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(-1, 50, 1))));
    }

    @Test
    @DisplayName("Aplică multiplicatorii de risc în funcție de cauzele profilului auto")
    void testAppliesRiskMultipliersBasedOnDriverProfileCauses() {
        // Multiplicator 1.5x (Vârstă <= 25 ȘI Experiență <= 2)
        assertEquals(1500.0, engine.calculatePremium(20, 1, 0, emptyClaims), 0.01);

        // Preț de bază 1000.0 (Experiența > 2 ani anulează riscul de vârstă tânără)
        assertEquals(1000.0, engine.calculatePremium(25, 3, 0, emptyClaims), 0.01);

        // Multiplicator 1.2x (Senior > 65 ani)
        assertEquals(1200.0, engine.calculatePremium(70, 30, 0, emptyClaims), 0.01);

        // Multiplicator 1.5x (Istoric încărcat cu >= 3 daune)
        var highRiskClaims = List.of(
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1)
        );
        assertEquals(1500.0, engine.calculatePremium(35, 10, 0, highRiskClaims), 0.01);
    }

    @Test
    @DisplayName("Calculează discountul de fidelitate analizând cauzele din istoricul de daune")
    void testEvaluatesLoyaltyDiscountBasedOnClaimCauses() {
        // Fără daune: Primește discountul de bază (0.05)
        assertEquals(950.0, engine.calculatePremium(35, 10, 1, emptyClaims), 0.01);

        // O singură daună veche (> 2 ani): Discount redus (0.02)
        var oldClaim = List.of(new AdvancedRiskEngine.Claim(0, 0, 3));
        assertEquals(980.0, engine.calculatePremium(35, 10, 1, oldClaim), 0.01);

        // O singură daună recentă (<= 2 ani): Discount anulat
        var recentClaim = List.of(new AdvancedRiskEngine.Claim(0, 0, 1));
        assertEquals(1000.0, engine.calculatePremium(35, 10, 1, recentClaim), 0.01);

        // Daune multiple: Discount anulat
        var multipleClaims = List.of(
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1)
        );
        assertEquals(1000.0, engine.calculatePremium(35, 10, 1, multipleClaims), 0.01);
    }

    @Test
    @DisplayName("Plafonează discountul maxim admis conform politicilor companiei")
    void testCapsLoyaltyDiscountAtMaximumAllowedThreshold() {
        // 6 ani * 0.05 = 0.30 -> Plafonat la maximul de 0.25 (750.0 lei)
        assertEquals(750.0, engine.calculatePremium(40, 10, 6, emptyClaims), 0.01);

        // 2 ani * 0.05 = 0.10 -> Neplafonat (900.0 lei)
        assertEquals(900.0, engine.calculatePremium(40, 10, 2, emptyClaims), 0.01);
    }
}