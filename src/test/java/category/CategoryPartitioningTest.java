package category;

import engine.AdvancedRiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pachet 2: Partiționarea pe Categorii - Validări și Calcule")
public class CategoryPartitioningTest {

    private AdvancedRiskEngine engine;
    private final List<AdvancedRiskEngine.Claim> emptyClaims = List.of();

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @ParameterizedTest(name = "Categoria: {4}")
    @CsvSource({
            "30, 10, 0, 1000.0, 'Adult Standard (Bază)'",
            "22, 1,  0, 1500.0, 'Tânăr Începător (Risc 1.5x)'",
            "24, 5,  0, 1000.0, 'Tânăr Experimentat (Fără penalizare vârstă)'",
            "70, 30, 0, 1200.0, 'Senior (Risc 1.2x)'",
            "40, 10, 6, 750.0,  'Fidelitate Maximă (Plafonare la 25%)'",
            "40, 10, 2, 900.0,  'Fidelitate Medie'"
    })
    @DisplayName("Calculează corect prima pentru categoriile principale de clienți valizi")
    void testCalculatesPremiumForValidCustomerCategories(int age, int exp, int loyalty, double expected, String description) {
        assertEquals(expected, engine.calculatePremium(age, exp, loyalty, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Evaluează corect categoriile bazate pe istoricul de daune")
    void testEvaluatesPremiumBasedOnClaimHistoryCategories() {
        // Categoria risc crescut (>= 3 daune) atrage multiplicatorul maxim
        var frequentClaims = List.of(
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1)
        );
        assertEquals(1500.0, engine.calculatePremium(35, 10, 0, frequentClaims), 0.01);

        // Categoria daună veche (> 2 ani) permite aplicarea unui discount parțial de loialitate
        var singleOldClaim = List.of(new AdvancedRiskEngine.Claim(0, 0, 3));
        assertEquals(980.0, engine.calculatePremium(35, 10, 1, singleOldClaim), 0.01);

        // Categoria daună recentă anulează complet discountul de loialitate
        var singleRecentClaim = List.of(new AdvancedRiskEngine.Claim(0, 0, 1));
        assertEquals(1000.0, engine.calculatePremium(35, 10, 1, singleRecentClaim), 0.01);

        // Mai multe daune (chiar dacă sub pragul de risc de 3) anulează discountul de loialitate
        var twoClaims = List.of(
                new AdvancedRiskEngine.Claim(0, 0, 1),
                new AdvancedRiskEngine.Claim(0, 0, 1)
        );
        assertEquals(1000.0, engine.calculatePremium(35, 10, 1, twoClaims), 0.01);
    }

    @Test
    @DisplayName("Respinge valori extreme sau invalide pentru atributele șoferului și ale daunelor")
    void testThrowsExceptionForInvalidCategoryBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(17, 0, 0, emptyClaims));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(100, 0, 0, emptyClaims));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, -1, 0, emptyClaims));
        // Experiența maximă logică la 30 ani este 12 (30 ani - 18 ani vârstă minimă = 12)
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 13, 0, emptyClaims));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, -1, emptyClaims));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 11, emptyClaims));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0, null));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(0, -1, 1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(0, 101, 1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(0, 50, -1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(-1, 50, 1))));
    }
}