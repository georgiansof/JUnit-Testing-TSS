import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("4. Anti-Mutanți (Mutation Coverage specific)")
class MutationCoverageTest {

    private AdvancedRiskEngine engine;
    private List<AdvancedRiskEngine.Claim> emptyClaims;

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
        emptyClaims = new ArrayList<>();
    }

    @Test
    @DisplayName("Kill Mutant - Loialitate exact la limita maximă admisă (10)")
    void killMutant_LoyaltyExactBoundary() {
        assertDoesNotThrow(() -> engine.calculatePremium(30, 5, 10, emptyClaims));
    }

    @Test
    @DisplayName("Kill Mutant - Precizia matematică a penalizării")
    void killMutant_SeverityCalculationMath() {
        var claim = new AdvancedRiskEngine.Claim(5000, 50, 1);
        double result = engine.calculatePremium(30, 5, 0, List.of(claim));
        assertEquals(1020.0, result, 0.0001);
    }

    @Test
    @DisplayName("Kill Mutant - Vina 0% e validă")
    void killMutant_FaultPercentageZeroIsValid() {
        var claim = new AdvancedRiskEngine.Claim(1000, 0, 1);
        assertDoesNotThrow(() -> engine.calculatePremium(30, 5, 0, List.of(claim)));
    }

    @Test
    @DisplayName("Kill Mutant - Vechimea daunei 0 (acum) e validă")
    void killMutant_YearsAgoZeroIsValid() {
        var claim = new AdvancedRiskEngine.Claim(1000, 50, 0);
        assertDoesNotThrow(() -> engine.calculatePremium(30, 5, 0, List.of(claim)));
    }

    @Test
    @DisplayName("Kill Mutant - Dauna de 0 lei e validă")
    void killMutant_AmountZeroIsValid() {
        var claim = new AdvancedRiskEngine.Claim(0, 50, 1);
        assertDoesNotThrow(() -> engine.calculatePremium(30, 5, 0, List.of(claim)));
    }
}