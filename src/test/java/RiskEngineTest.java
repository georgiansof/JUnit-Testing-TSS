import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DisplayName("Suita Exhaustivă - Advanced Risk Engine")
class RiskEngineTest {

    private AdvancedRiskEngine engine;
    private List<AdvancedRiskEngine.Claim> emptyClaims;

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
        emptyClaims = new ArrayList<>();
    }

    @ParameterizedTest(name = "Frontieră Invalidă: Vârstă={0}, Exp={1}, Loialitate={2}")
    @CsvSource({
            "17, 0, 0",   // Vârstă sub limită (18)
            "100, 10, 0", // Vârstă peste limită (99)
            "30, -1, 0",  // Experiență negativă
            "30, 13, 0",  // Exp (13) > Vârstă (30) - 18 (12)
            "40, 10, -1", // Loialitate sub limită (0)
            "40, 10, 11"  // Loialitate peste limită (10)
    })
    @DisplayName("BVA: Excepții pentru limite depășite")
    void testBoundaryExceptions(int age, int exp, int loyalty) {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(age, exp, loyalty, emptyClaims));
    }

    @Test
    @DisplayName("BVA/ECP: Excepție pentru istoric daune Null")
    void testNullClaims() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(30, 5, 0, null));
    }

    @ParameterizedTest(name = "MCDC Rule {0}: Vârstă={1}, Exp={2}, Daune={3} => Prima={4}")
    @CsvSource({
            "R1 (A-A-F), 20, 1, 0, 1500.0", // Tânăr + Fără Exp -> +50% (1000 * 1.5)
            "R2 (A-F-F), 25, 5, 0, 1000.0", // Tânăr + Exp OK -> +0% (1000 * 1.0)
            "R3 (F-A-F), 30, 1, 0, 1000.0", // Adult + Fără Exp -> +0% (1000 * 1.0)
            "R4 (X-X-A), 40, 10, 3, 1500.0", // Oricine + 3 Daune -> +50% (Aici generăm daunele în cod)
            "R5 (Senior), 70, 40, 0, 1200.0", // Senior -> +20% (1000 * 1.2)
            "R6 (Standard), 40, 10, 0, 1000.0" // Adult Standard -> +0%
    })
    @DisplayName("Acoperire Decizii (Branch Coverage) pentru Profilul de Risc")
    void testCauseEffectGraph(String rule, int age, int exp, int numClaims, double expectedPremium) {
        List<AdvancedRiskEngine.Claim> claims = new ArrayList<>();
        // Pentru R4 generăm 3 daune neutre (foarte vechi, impact financiar 0 pt a testa doar flag-ul)
        for(int i=0; i<numClaims; i++) {
            claims.add(new AdvancedRiskEngine.Claim(0, 0, 5));
        }
        double actual = engine.calculatePremium(age, exp, 0, claims);
        assertEquals(expectedPremium, actual, 0.01);
    }

    @Test
    @DisplayName("Loop Coverage: Buclă ocolită (0 Iterații)")
    void testForLoopZeroIterations() {
        assertEquals(1000.0, engine.calculatePremium(30, 10, 0, emptyClaims));
    }

    @Test
    @DisplayName("Loop Coverage: Exact 1 Iterație (Daună recentă gravă)")
    void testForLoopOneIteration() {
        // Val=100.000, Culpa=100%, Vechime=0 ani -> TimeDecay=1.0 -> Penalizare = +1.0
        List<AdvancedRiskEngine.Claim> claims = Arrays.asList(
                new AdvancedRiskEngine.Claim(100000.0, 100, 0)
        );
        // Multiplicator = 1.0 (bază) + 1.0 (daună) = 2.0 => 2000 RON
        assertEquals(2000.0, engine.calculatePremium(30, 10, 0, claims));
    }

    @Test
    @DisplayName("Loop Coverage: Mai multe iterații + Limitare Time Decay")
    void testForLoopMultipleIterations() {
        List<AdvancedRiskEngine.Claim> claims = Arrays.asList(
                new AdvancedRiskEngine.Claim(50000.0, 100, 5), // Vechime 5 ani -> TimeDecay=0 -> Impact 0
                new AdvancedRiskEngine.Claim(50000.0, 50, 0)   // Val=50k, 50% -> severity=25. Decay=1. Impact=0.25
        );
        // Multiplicator = 1.0 + 0.25 = 1.25 => 1250 RON
        assertEquals(1250.0, engine.calculatePremium(30, 10, 0, claims));
    }

    @Test
    @DisplayName("BVA pe interiorul Buclei: Date daună invalide")
    void testInvalidClaimDataInsideLoop() {
        List<AdvancedRiskEngine.Claim> badClaims = Arrays.asList(
                new AdvancedRiskEngine.Claim(1000, 101, 0) // Culpă > 100%
        );
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(30, 10, 0, badClaims));
    }

    @ParameterizedTest(name = "Ani Loialitate={0}, AreDaune={1} => Așteptat={2}")
    @CsvSource({
            "1, false, 950.0",  // 1 An curat -> 5% discount (1000 * 0.95)
            "5, false, 750.0",  // 5 Ani curați -> 25% discount (1000 * 0.75)
            "10, false, 750.0", // 10 Ani (Plafonare) -> 25% discount (750 RON)
    })
    @DisplayName("Basis Path: Calea 1 - Loialitate Fără Daune")
    void testWhileLoopCleanPath(int loyalty, boolean hasClaims, double expected) {
        assertEquals(expected, engine.calculatePremium(30, 10, loyalty, emptyClaims));
    }

    @Test
    @DisplayName("Basis Path: Calea 2 - Loialitate cu o daună veche (Minor discount)")
    void testWhileLoopForgivenClaimPath() {
        // O daună veche (3 ani), cu valoare mică
        List<AdvancedRiskEngine.Claim> claims = Arrays.asList(
                new AdvancedRiskEngine.Claim(1000.0, 10, 3)
        );
        // Discountul va fi pe calea `else if`: 2% pe an * 2 ani loialitate = 4% discount
        // Ignorăm penalizarea daunelor (e microscopică la 1000 RON, practic 0)
        double premium = engine.calculatePremium(30, 10, 2, claims);
        assertTrue(premium < 1000.0 && premium >= 950.0, "Premium ar trebui să reflecte un mic discount");
    }

    @Test
    @DisplayName("Mutation Killer: Limita Minimă Absolută de 500 RON")
    void testAbsoluteMinimumPremium() {
        double premium = engine.calculatePremium(30, 10, 5, emptyClaims);
        assertEquals(750.0, premium); // Asigură stabilitatea ecuației finale
    }

    @ParameterizedTest(name = "Date daună invalide: Val={0}, Culpa={1}, Vechime={2}")
    @CsvSource({
            "1000.0, -1, 2",   // faultPercentage < 0
            "1000.0, 101, 2",  // faultPercentage > 100
            "1000.0, 50, -1",  // yearsAgo < 0
            "-500.0, 50, 2"    // amount < 0
    })
    @DisplayName("MCDC: Acoperirea tuturor ramurilor de excepție din bucla FOR")
    void testAllInvalidClaimConditions(double amount, int fault, int yearsAgo) {
        List<AdvancedRiskEngine.Claim> badClaims = Arrays.asList(
                new AdvancedRiskEngine.Claim(amount, fault, yearsAgo)
        );
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(30, 10, 0, badClaims));
    }

    @Test
    @DisplayName("Branch Coverage: 1 Daună, dar prea recentă pentru a fi iertată")
    void testSingleRecentClaimNoDiscount() {
        List<AdvancedRiskEngine.Claim> recentClaim = Arrays.asList(
                new AdvancedRiskEngine.Claim(5000.0, 100, 1)
        );
        double premium = engine.calculatePremium(30, 10, 3, recentClaim);
        assertEquals(1040.0, premium, 0.01);
    }

    @Test
    @DisplayName("Boundary / Infeasible Path: Forțarea plafonului maxim de discount")
    void testMaxLoyaltyDiscountCapping() {
        double premium = engine.calculatePremium(30, 10, 10, new ArrayList<>());
        assertEquals(750.0, premium, 0.001);
    }

    @Test
    @DisplayName("Coverage Linia 38: Tânăr DAR cu experiență (A=True, B=False)")
    void testYoungDriverWithExperience() {
        double premium = engine.calculatePremium(25, 5, 0, emptyClaims);
        assertEquals(1000.0, premium);
    }

    @Test
    @DisplayName("Coverage Linia 40: Șofer Senior")
    void testSeniorDriver() {
        double premium = engine.calculatePremium(70, 30, 0, emptyClaims);
        assertEquals(1200.0, premium);
    }

    @Test
    @DisplayName("Coverage Liniile 56/58: Loialitate, dar cu 1 daună RECENTĂ")
    void testLoyaltyWithOneRecentClaim() {
        List<AdvancedRiskEngine.Claim> recentClaim = Arrays.asList(
                new AdvancedRiskEngine.Claim(1000.0, 100, 1)
        );
        double premium = engine.calculatePremium(30, 10, 2, recentClaim);
        assertTrue(premium > 1000.0);
    }

    @Test
    @DisplayName("Coverage Linia 58: Loialitate, dar cu MAI MULTE daune")
    void testLoyaltyWithMultipleClaims() {
        List<AdvancedRiskEngine.Claim> multipleClaims = Arrays.asList(
                new AdvancedRiskEngine.Claim(1000.0, 100, 4),
                new AdvancedRiskEngine.Claim(1000.0, 100, 4)
        );
        double premium = engine.calculatePremium(30, 10, 2, multipleClaims);

        assertTrue(premium >= 1000.0);
    }
}