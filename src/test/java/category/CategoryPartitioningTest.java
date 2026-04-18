package category;

import engine.AdvancedRiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pachet 2: Partiționarea pe Categorii - Validări și Calcule")
public class CategoryPartitioningTest {

    private AdvancedRiskEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @ParameterizedTest(name = "[{index}] {0} -> age:{1}, exp:{2}, loy:{3}")
    @MethodSource("provideAll51TestCases")
    void testCategoryPartitioning(String testName, int age, int exp, int loy, List<AdvancedRiskEngine.Claim> claims, Double expectedPremium) {
        if (expectedPremium == null) {
            assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(age, exp, loy, claims));
        } else {
            assertEquals(expectedPremium, engine.calculatePremium(age, exp, loy, claims), 0.01);
        }
    }

    private static Stream<Arguments> provideAll51TestCases() {
        var claimR = new AdvancedRiskEngine.Claim(1000, 100, 1);
        var claimV = new AdvancedRiskEngine.Claim(1000, 100, 3);
        var claimFaultN = new AdvancedRiskEngine.Claim(1000, -1, 1);
        var claimFaultM = new AdvancedRiskEngine.Claim(1000, 101, 1);
        var claimYearsN = new AdvancedRiskEngine.Claim(1000, 50, -1);
        var claimAmountN = new AdvancedRiskEngine.Claim(-1, 50, 1);
        var zero = List.<AdvancedRiskEngine.Claim>of();
        var oR = List.of(claimR);
        var oV = List.of(claimV);
        var doua = List.of(claimR, claimR);
        var trei = List.of(claimR, claimR, claimR);

        return Stream.of(
                Arguments.of("ERR_DA_Min", 17, 0, 0, zero, null),
                Arguments.of("ERR_DA_Max", 100, 0, 0, zero, null),
                Arguments.of("ERR_EX_Neg", 30, -1, 0, zero, null),
                Arguments.of("ERR_EX_PreaMare", 30, 13, 0, zero, null),
                Arguments.of("ERR_LY_Neg", 30, 1, -1, zero, null),
                Arguments.of("ERR_LY_Max", 30, 1, 11, zero, null),
                Arguments.of("ERR_CC_Null", 30, 1, 0, null, null),
                Arguments.of("ERR_CV_FaultNeg", 30, 1, 0, List.of(claimFaultN), null),
                Arguments.of("ERR_CV_FaultMax", 30, 1, 0, List.of(claimFaultM), null),
                Arguments.of("ERR_CV_YearsNeg", 30, 1, 0, List.of(claimYearsN), null),
                Arguments.of("ERR_CV_AmountNeg", 30, 1, 0, List.of(claimAmountN), null),
                Arguments.of("Tanar_ExpMica_FaraLoy_Zero", 22, 1, 0, zero, 1500.0),
                Arguments.of("Tanar_ExpMica_FaraLoy_1Rec", 22, 1, 0, oR, 1508.0),
                Arguments.of("Tanar_ExpMica_FaraLoy_1Vec", 22, 1, 0, oV, 1504.0),
                Arguments.of("Tanar_ExpMica_FaraLoy_2Daune", 22, 1, 0, doua, 1516.0),
                Arguments.of("Tanar_ExpMica_FaraLoy_3Daune", 22, 1, 0, trei, 1524.0),
                Arguments.of("Tanar_ExpMica_LoyMax_Zero", 22, 1, 5, zero, 1250.0),
                Arguments.of("Tanar_ExpMica_LoyMax_1Rec", 22, 1, 5, oR, 1508.0),
                Arguments.of("Tanar_ExpMica_LoyMax_1Vec", 22, 1, 5, oV, 1404.0),
                Arguments.of("Tanar_ExpMica_LoyMax_2Daune", 22, 1, 5, doua, 1516.0),
                Arguments.of("Tanar_ExpMica_LoyMax_3Daune", 22, 1, 5, trei, 1524.0),
                Arguments.of("Tanar_ExpMare_FaraLoy_Zero", 25, 5, 0, zero, 1000.0),
                Arguments.of("Tanar_ExpMare_FaraLoy_1Rec", 25, 5, 0, oR, 1008.0),
                Arguments.of("Tanar_ExpMare_FaraLoy_1Vec", 25, 5, 0, oV, 1004.0),
                Arguments.of("Tanar_ExpMare_FaraLoy_2Daune", 25, 5, 0, doua, 1016.0),
                Arguments.of("Tanar_ExpMare_FaraLoy_3Daune", 25, 5, 0, trei, 1524.0),
                Arguments.of("Tanar_ExpMare_LoyMax_Zero", 25, 5, 5, zero, 750.0),
                Arguments.of("Tanar_ExpMare_LoyMax_1Rec", 25, 5, 5, oR, 1008.0),
                Arguments.of("Tanar_ExpMare_LoyMax_1Vec", 25, 5, 5, oV, 904.0),
                Arguments.of("Tanar_ExpMare_LoyMax_2Daune", 25, 5, 5, doua, 1016.0),
                Arguments.of("Tanar_ExpMare_LoyMax_3Daune", 25, 5, 5, trei, 1524.0),
                Arguments.of("Adult_FaraLoy_Zero", 40, 10, 0, zero, 1000.0),
                Arguments.of("Adult_FaraLoy_1Rec", 40, 10, 0, oR, 1008.0),
                Arguments.of("Adult_FaraLoy_1Vec", 40, 10, 0, oV, 1004.0),
                Arguments.of("Adult_FaraLoy_2Daune", 40, 10, 0, doua, 1016.0),
                Arguments.of("Adult_FaraLoy_3Daune", 40, 10, 0, trei, 1524.0),
                Arguments.of("Adult_LoyMax_Zero", 40, 10, 5, zero, 750.0),
                Arguments.of("Adult_LoyMax_1Rec", 40, 10, 5, oR, 1008.0),
                Arguments.of("Adult_LoyMax_1Vec", 40, 10, 5, oV, 904.0),
                Arguments.of("Adult_LoyMax_2Daune", 40, 10, 5, doua, 1016.0),
                Arguments.of("Adult_LoyMax_3Daune", 40, 10, 5, trei, 1524.0),
                Arguments.of("Senior_FaraLoy_Zero", 70, 5, 0, zero, 1200.0),
                Arguments.of("Senior_FaraLoy_1Rec", 70, 5, 0, oR, 1208.0),
                Arguments.of("Senior_FaraLoy_1Vec", 70, 5, 0, oV, 1204.0),
                Arguments.of("Senior_FaraLoy_2Daune", 70, 5, 0, doua, 1216.0),
                Arguments.of("Senior_FaraLoy_3Daune", 70, 5, 0, trei, 1524.0),
                Arguments.of("Senior_LoyMax_Zero", 70, 5, 5, zero, 950.0),
                Arguments.of("Senior_LoyMax_1Rec", 70, 5, 5, oR, 1208.0),
                Arguments.of("Senior_LoyMax_1Vec", 70, 5, 5, oV, 1104.0),
                Arguments.of("Senior_LoyMax_2Daune", 70, 5, 5, doua, 1216.0),
                Arguments.of("Senior_LoyMax_3Daune", 70, 5, 5, trei, 1524.0)
        );
    }
}