import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("3. Teste de Frontieră (BVA)")
class BoundaryTest {

    private AdvancedRiskEngine engine;
    private List<AdvancedRiskEngine.Claim> emptyClaims;

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
        emptyClaims = new ArrayList<>();
    }

    @Test
    @DisplayName("Frontieră Vârstă: Minim absolut (18)")
    void age_18_minValid() {
        assertEquals(1500.0, engine.calculatePremium(18, 0, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Vârstă: Maxim absolut (99)")
    void age_99_maxValid() {
        assertEquals(1200.0, engine.calculatePremium(99, 10, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Risc Tineri: 25 ani, 2 ani exp -> La limită, Risc Crescut")
    void youngDriver_age25_exp2_atThreshold() {
        assertEquals(1500.0, engine.calculatePremium(25, 2, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Risc Tineri: 25 ani, 3 ani exp -> Scapă de risc din cauza experienței")
    void youngDriver_age25_exp3_justAboveThreshold() {
        assertEquals(1000.0, engine.calculatePremium(25, 3, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Risc Tineri: 26 ani, 2 ani exp -> Scapă de risc din cauza vârstei")
    void adultDriver_age26_exp2_justAboveAgeThreshold() {
        assertEquals(1000.0, engine.calculatePremium(26, 2, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Vârstnici: 65 ani -> Încă adult normal")
    void adultDriver_age65_atThreshold() {
        assertEquals(1000.0, engine.calculatePremium(65, 10, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Vârstnici: 66 ani -> Intră la risc seniori")
    void seniorDriver_age66_justAboveThreshold() {
        assertEquals(1200.0, engine.calculatePremium(66, 10, 0, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Daune Multiple: 2 daune -> Nu se aplică multiplicatorul de x1.5")
    void claimsCount_2_belowThreshold() {
        double p = engine.calculatePremium(40, 10, 0, List.of(
                new AdvancedRiskEngine.Claim(100, 50, 1),
                new AdvancedRiskEngine.Claim(100, 50, 1)
        ));
        assertTrue(p >= 1000.0 && p < 1500.0);
    }

    @Test
    @DisplayName("Frontieră Daune Multiple: 3 daune -> Lovește multiplicatorul de x1.5")
    void claimsCount_3_atThreshold() {
        double p = engine.calculatePremium(40, 10, 0, List.of(
                new AdvancedRiskEngine.Claim(100, 50, 1),
                new AdvancedRiskEngine.Claim(100, 50, 1),
                new AdvancedRiskEngine.Claim(100, 50, 1)
        ));
        assertTrue(p >= 1500.0);
    }

    @Test
    @DisplayName("Frontieră Loialitate: Plafonul maxim atins la 5 ani")
    void loyalty_5_maxDiscountThreshold() {
        assertEquals(750.0, engine.calculatePremium(30, 5, 5, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Loialitate: Peste 5 ani (6) -> Plafonul rămâne neschimbat")
    void loyalty_6_aboveDiscountThreshold() {
        assertEquals(750.0, engine.calculatePremium(30, 5, 6, emptyClaims), 0.01);
    }

    @Test
    @DisplayName("Frontieră Vechime Daună: Exact 2 ani -> Niciun discount")
    void singleClaim_yearsAgo2_noDiscount() {
        assertTrue(engine.calculatePremium(30, 5, 5, List.of(
                new AdvancedRiskEngine.Claim(100, 50, 2)
        )) > 1000.0);
    }

    @Test
    @DisplayName("Frontieră Vechime Daună: Trecut de 2 ani (3) -> Se aplică discountul")
    void singleClaim_yearsAgo3_discountApplies() {
        assertTrue(engine.calculatePremium(30, 5, 5, List.of(
                new AdvancedRiskEngine.Claim(100, 50, 3)
        )) < 1000.0);
    }
}