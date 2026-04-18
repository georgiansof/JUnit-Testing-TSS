package causeeffect;

import engine.AdvancedRiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pachet 3: Analiză Cauză-Efect - Reguli de Business și Decizii")
class CauseEffectTest {
    private AdvancedRiskEngine engine;

    private static final AdvancedRiskEngine.Claim DAUNA_RECENTA =
            new AdvancedRiskEngine.Claim(0, 0, 1);
    private static final AdvancedRiskEngine.Claim DAUNA_VECHE =
            new AdvancedRiskEngine.Claim(0, 0, 3);

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @Nested
    @DisplayName("Ef0: IllegalArgumentException — cauze de validare")
    class CauzeInvalide {

        @Test
        @DisplayName("Ce1a: driverAge < 18 → Ef0")
        void ce1a_varstaSub18() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(17, 0, 0, List.of()));
        }

        @Test
        @DisplayName("Ce1b: driverAge > 99 → Ef0")
        void ce1b_varstaOver99() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(100, 0, 0, List.of()));
        }

        @Test
        @DisplayName("Ce2a: experienceYears < 0 → Ef0")
        void ce2a_experientaNegativa() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, -1, 0, List.of()));
        }

        @Test
        @DisplayName("Ce2b: experienceYears > driverAge-18 → Ef0")
        void ce2b_experientaDepasisteMaxim() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 13, 0, List.of()));
        }

        @Test
        @DisplayName("Ce3a: loyaltyYears < 0 → Ef0")
        void ce3a_loialitataNegativa() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, -1, List.of()));
        }

        @Test
        @DisplayName("Ce3b: loyaltyYears > 10 → Ef0")
        void ce3b_loialitateDepasisteMaxim() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 11, List.of()));
        }

        @Test
        @DisplayName("Ce4: claims == null → Ef0")
        void ce4_claimsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 0, null));
        }

        @Test
        @DisplayName("Ce5: daună cu faultPercentage < 0 → Ef0")
        void ce5_daunaInvalida() {
            var daunaRea = new AdvancedRiskEngine.Claim(1000, -1, 1);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 0, List.of(daunaRea)));
        }
    }

    static Stream<Arguments> tabelDeDecizie() {
        var zero = List.<AdvancedRiskEngine.Claim>of();
        var unaVeche = List.of(DAUNA_VECHE);
        var doua = List.of(DAUNA_RECENTA, DAUNA_RECENTA);
        var trei = List.of(DAUNA_RECENTA, DAUNA_RECENTA, DAUNA_RECENTA);

        return Stream.of(
                Arguments.of("Col1_Tanar_ExpMica_ZeroDaune_Loyalty",
                        22, 1, 5, zero, 1250.0),
                Arguments.of("Col2_Tanar_ExpMica_DaunaVeche_Loyalty",
                        22, 1, 5, unaVeche, 1400.0),
                Arguments.of("Col3_Tanar_ExpMica_2Daune_FaraDiscount",
                        22, 1, 0, doua, 1500.0), Arguments.of("Col4_Tanar_ExpMare_ZeroDaune_Loyalty",
                        25, 5, 5, zero, 750.0),
                Arguments.of("Col5_Tanar_ExpMare_DaunaVeche_Loyalty",
                        25, 5, 5, unaVeche, 900.0),
                Arguments.of("Col6_Tanar_ExpMare_2Daune_FaraDiscount",
                        25, 5, 0, doua, 1000.0), Arguments.of("Col7_Adult_ZeroDaune_Loyalty",
                        40, 10, 5, zero, 750.0),
                Arguments.of("Col8_Adult_DaunaVeche_Loyalty",
                        40, 10, 5, unaVeche, 900.0),
                Arguments.of("Col9_Adult_2Daune_FaraDiscount",
                        40, 10, 0, doua, 1000.0), Arguments.of("Col10_Senior_ZeroDaune_Loyalty",
                        70, 1, 5, zero, 950.0),
                Arguments.of("Col11_Senior_DaunaVeche_Loyalty",
                        70, 1, 5, unaVeche, 1100.0),
                Arguments.of("Col12_Senior_2Daune_FaraDiscount",
                        70, 1, 0, doua, 1200.0), Arguments.of("Col13_3SauMaiMulteDaune_HighRisk",
                        40, 10, 0, trei, 1500.0)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tabelDeDecizie")
    @DisplayName("Tabel decizie cauză-efect — coloanele 1-13")
    void tabelDeDecizie(String label, int age, int exp, int loyalty,
                        List<AdvancedRiskEngine.Claim> claims,
                        double expectedPremium) {
        double result = engine.calculatePremium(age, exp, loyalty, claims);
        assertEquals(expectedPremium, result, 0.001,
                "Premium incorect pentru scenariul: " + label);
    }

    @Nested
    @DisplayName("Verificare constrângeri graf")
    class VerificareConstrangeri {

        @Test
        @DisplayName("E(C1,C3): tânăr+exp mică nu poate fi și senior — numai Ef1 activ")
        void constrangere_C1_C3_exclusiv() {
            double result = engine.calculatePremium(22, 1, 0, List.of());
            assertEquals(1500.0, result, 0.001,
                    "Ef1 și Ef2 nu trebuie activate simultan");
        }

        @Test
        @DisplayName("M(Ef1,Ef2): senior cu 3+ daune → Ef1 activ, Ef2 mascat")
        void constrangere_M_Ef1_mascheaza_Ef2() {
            var trei = List.of(
                    new AdvancedRiskEngine.Claim(0, 0, 5),
                    new AdvancedRiskEngine.Claim(0, 0, 5),
                    new AdvancedRiskEngine.Claim(0, 0, 5)
            );
            double result = engine.calculatePremium(70, 1, 0, trei);
            assertEquals(1500.0, result, 0.001,
                    "Senior cu 3 daune: riskMultiplier=1.50 (Ef1 mascat Ef2)");
        }

        @Test
        @DisplayName("E(C4,C5,C6): 3 daune → discount imposibil (Ef5 forțat)")
        void constrangere_E_C4_C5_C6_exclusiv() {
            var trei = List.of(
                    new AdvancedRiskEngine.Claim(0, 0, 5),
                    new AdvancedRiskEngine.Claim(0, 0, 5),
                    new AdvancedRiskEngine.Claim(0, 0, 5)
            );


            double result = engine.calculatePremium(40, 10, 5, trei);
            assertEquals(1500.0, result, 0.001,
                    "C4=1 exclude C5 și C6: loyaltyDiscount=0 chiar cu loyalty=5");
        }
    }
}