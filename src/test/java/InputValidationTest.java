import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("1. Validare parametri de intrare")
class InputValidationTest {

    private AdvancedRiskEngine engine;
    private List<AdvancedRiskEngine.Claim> emptyClaims;

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
        emptyClaims = new ArrayList<>();
    }

    @ParameterizedTest(name = "Vârstă={0}, Exp={1}, Loialitate={2} -> IllegalArgumentException")
    @CsvSource({
            "17, 0, 0",   // driverAge < 18
            "100, 0, 0",  // driverAge > 99
            "30, -1, 0",  // experience < 0
            "30, 13, 0",  // experience > age - 18
            "30, 0, -1",  // loyalty < 0
            "30, 0, 11"   // loyalty > 10
    })
    @DisplayName("Parametri șofer invalizi")
    void invalidDriverParams(int age, int exp, int loyalty) {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(age, exp, loyalty, emptyClaims));
    }

    @Test
    @DisplayName("Daune (Claims) invalide -> IllegalArgumentException")
    void invalidClaims() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0, null));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0,
                List.of(new AdvancedRiskEngine.Claim(100, -1, 1))));
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0,
                List.of(new AdvancedRiskEngine.Claim(100, 101, 1))));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0,
                List.of(new AdvancedRiskEngine.Claim(100, 50, -1))));

        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 0, 0,
                List.of(new AdvancedRiskEngine.Claim(-1, 50, 1))));
    }
}