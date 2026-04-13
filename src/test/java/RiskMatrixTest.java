import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("2. Matricea STAS (58 Cazuri Partajate)")
class RiskMatrixTest {

    private AdvancedRiskEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @ParameterizedTest(name = "[{index}] {0} -> age:{1}, exp:{2}, loy:{3}")
    @MethodSource("provideAll58TestCases")
    @DisplayName("Testare exhaustivă reguli de business")
    void testCategoryPartitioning(String testName, int age, int exp, int loy, List<AdvancedRiskEngine.Claim> claims, Double expectedPremium) {
        if (expectedPremium == null) {
            assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(age, exp, loy, claims));
        } else {
            assertEquals(expectedPremium, engine.calculatePremium(age, exp, loy, claims), 0.01);
        }
    }

    private static Stream<Arguments> provideAll58TestCases() {
        var claimR = new AdvancedRiskEngine.Claim(1000, 100, 1);
        var claimV = new AdvancedRiskEngine.Claim(1000, 100, 3);
        var claimInv = new AdvancedRiskEngine.Claim(1000, -1, 1);
        var zero = List.<AdvancedRiskEngine.Claim>of();
        var oR = List.of(claimR);
        var oV = List.of(claimV);
        var doua = List.of(claimR, claimR);
        var trei = List.of(claimR, claimR, claimR);

        return Stream.of(
                // --- VALIDĂRI ---
                Arguments.of("ERR_DA_Min", 17, 0, 0, zero, null),
                Arguments.of("ERR_DA_Max", 100, 0, 0, zero, null),
                Arguments.of("ERR_EX_Neg", 30, -1, 0, zero, null),
                Arguments.of("ERR_EX_PreaMare", 30, 13, 0, zero, null),
                Arguments.of("ERR_LY_Neg", 30, 1, -1, zero, null),
                Arguments.of("ERR_LY_Max", 30, 1, 11, zero, null),
                Arguments.of("ERR_CC_Null", 30, 1, 0, null, null),
                Arguments.of("ERR_CV_Invalid", 30, 1, 0, List.of(claimInv), null),

                // --- BAZĂ FĂRĂ DAUNE ---
                Arguments.of("Tanar_ExpMica_FaraLoy_Zero", 22, 1, 0, zero, 1500.0),
                Arguments.of("Tanar_ExpMica_LoyMax_Zero", 22, 1, 5, zero, 1250.0),
                Arguments.of("Adult_ExpMica_FaraLoy_Zero", 30, 1, 0, zero, 1000.0),
                Arguments.of("Adult_ExpMica_LoyMax_Zero", 30, 1, 5, zero, 750.0),
                Arguments.of("Adult_ExpMare_FaraLoy_Zero", 40, 10, 0, zero, 1000.0),
                Arguments.of("Adult_ExpMare_LoyMax_Zero", 40, 10, 5, zero, 750.0),
                Arguments.of("Senior_ExpMica_FaraLoy_Zero", 70, 1, 0, zero, 1200.0),
                Arguments.of("Senior_ExpMica_LoyMax_Zero", 70, 1, 5, zero, 950.0),
                Arguments.of("Senior_ExpMare_FaraLoy_Zero", 70, 40, 0, zero, 1200.0),
                Arguments.of("Senior_ExpMare_LoyMax_Zero", 70, 40, 5, zero, 950.0),

                // --- 1 DAUNĂ RECENTĂ ---
                Arguments.of("Tanar_ExpMica_FaraLoy_1Rec", 22, 1, 0, oR, 1508.0),
                Arguments.of("Tanar_ExpMica_LoyMax_1Rec", 22, 1, 5, oR, 1508.0),
                Arguments.of("Adult_ExpMica_FaraLoy_1Rec", 30, 1, 0, oR, 1008.0),
                Arguments.of("Adult_ExpMica_LoyMax_1Rec", 30, 1, 5, oR, 1008.0),
                Arguments.of("Adult_ExpMare_FaraLoy_1Rec", 40, 10, 0, oR, 1008.0),
                Arguments.of("Adult_ExpMare_LoyMax_1Rec", 40, 10, 5, oR, 1008.0),
                Arguments.of("Senior_ExpMica_FaraLoy_1Rec", 70, 1, 0, oR, 1208.0),
                Arguments.of("Senior_ExpMica_LoyMax_1Rec", 70, 1, 5, oR, 1208.0),
                Arguments.of("Senior_ExpMare_FaraLoy_1Rec", 70, 40, 0, oR, 1208.0),
                Arguments.of("Senior_ExpMare_LoyMax_1Rec", 70, 40, 5, oR, 1208.0),

                // --- 1 DAUNĂ VECHE ---
                Arguments.of("Tanar_ExpMica_FaraLoy_1Vechi", 22, 1, 0, oV, 1504.0),
                Arguments.of("Tanar_ExpMica_LoyMax_1Vechi", 22, 1, 5, oV, 1404.0),
                Arguments.of("Adult_ExpMica_FaraLoy_1Vechi", 30, 1, 0, oV, 1004.0),
                Arguments.of("Adult_ExpMica_LoyMax_1Vechi", 30, 1, 5, oV, 904.0),
                Arguments.of("Adult_ExpMare_FaraLoy_1Vechi", 40, 10, 0, oV, 1004.0),
                Arguments.of("Adult_ExpMare_LoyMax_1Vechi", 40, 10, 5, oV, 904.0),
                Arguments.of("Senior_ExpMica_FaraLoy_1Vechi", 70, 1, 0, oV, 1204.0),
                Arguments.of("Senior_ExpMica_LoyMax_1Vechi", 70, 1, 5, oV, 1104.0),
                Arguments.of("Senior_ExpMare_FaraLoy_1Vechi", 70, 40, 0, oV, 1204.0),
                Arguments.of("Senior_ExpMare_LoyMax_1Vechi", 70, 40, 5, oV, 1104.0),

                // --- 2 DAUNE ---
                Arguments.of("Tanar_ExpMica_FaraLoy_2Daune", 22, 1, 0, doua, 1516.0),
                Arguments.of("Tanar_ExpMica_LoyMax_2Daune", 22, 1, 5, doua, 1516.0),
                Arguments.of("Adult_ExpMica_FaraLoy_2Daune", 30, 1, 0, doua, 1016.0),
                Arguments.of("Adult_ExpMica_LoyMax_2Daune", 30, 1, 5, doua, 1016.0),
                Arguments.of("Adult_ExpMare_FaraLoy_2Daune", 40, 10, 0, doua, 1016.0),
                Arguments.of("Adult_ExpMare_LoyMax_2Daune", 40, 10, 5, doua, 1016.0),
                Arguments.of("Senior_ExpMica_FaraLoy_2Daune", 70, 1, 0, doua, 1216.0),
                Arguments.of("Senior_ExpMica_LoyMax_2Daune", 70, 1, 5, doua, 1216.0),
                Arguments.of("Senior_ExpMare_FaraLoy_2Daune", 70, 40, 0, doua, 1216.0),
                Arguments.of("Senior_ExpMare_LoyMax_2Daune", 70, 40, 5, doua, 1216.0),

                // --- 3 DAUNE ---
                Arguments.of("Tanar_ExpMica_FaraLoy_3Daune", 22, 1, 0, trei, 1524.0),
                Arguments.of("Tanar_ExpMica_LoyMax_3Daune", 22, 1, 5, trei, 1524.0),
                Arguments.of("Adult_ExpMica_FaraLoy_3Daune", 30, 1, 0, trei, 1524.0),
                Arguments.of("Adult_ExpMica_LoyMax_3Daune", 30, 1, 5, trei, 1524.0),
                Arguments.of("Adult_ExpMare_FaraLoy_3Daune", 40, 10, 0, trei, 1524.0),
                Arguments.of("Adult_ExpMare_LoyMax_3Daune", 40, 10, 5, trei, 1524.0),
                Arguments.of("Senior_ExpMica_FaraLoy_3Daune", 70, 1, 0, trei, 1524.0),
                Arguments.of("Senior_ExpMica_LoyMax_3Daune", 70, 1, 5, trei, 1524.0),
                Arguments.of("Senior_ExpMare_FaraLoy_3Daune", 70, 40, 0, trei, 1524.0),
                Arguments.of("Senior_ExpMare_LoyMax_3Daune", 70, 40, 5, trei, 1524.0)
        );
    }
}