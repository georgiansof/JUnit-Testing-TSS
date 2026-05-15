package cfg;

import engine.AdvancedRiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Pachet 5: CFG")
public class CfgTest {
    private AdvancedRiskEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @Test
    void path1_VarstaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(17, 0, 0, List.of()));
    }

    @Test
    void path2_ExperientaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(20, 5, 0, List.of()));
    }

    @Test
    void path3_LoialitateInvalida() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 11, List.of()));
    }

    @Test
    void path4_DauneNull() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculatePremium(30, 5, 0, null));
    }

    @Test
    void path5_RiscStandard() {
        assertEquals(1000.0, engine.calculatePremium(30, 5, 0, List.of()));
    }

    @Test
    void path6_RiscRidicatTanar() {
        assertEquals(1500.0, engine.calculatePremium(20, 1, 0, List.of()));
    }

    @Test
    void path7_RiscRidicatVarstnic() {
        assertEquals(1200.0, engine.calculatePremium(70, 40, 0, List.of()));
    }

    @Test
    void path8_DateDaunaInvalide() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculatePremium(30, 5, 0, List.of(new AdvancedRiskEngine.Claim(1000, -10, 1)))
        );
    }

    @Test
    void path9_DaunaValidaFaraReducere() {
        assertEquals(1008.0, engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(1000, 100, 1)))
        );
    }

    @Test
    void path10_ReducereFaraDaune() {
        assertEquals(950.0, engine.calculatePremium(30, 5, 1, List.of()));
    }

    @Test
    void path11_ReducereDaunaVeche() {
        assertEquals(984.0, engine.calculatePremium(30, 5, 1,
                List.of(new AdvancedRiskEngine.Claim(1000, 100, 3)))
        );
    }

    @Test
    void path12_FaraReducereDaunaRecenta() {
        assertEquals(1008.0, engine.calculatePremium(30, 5, 1,
                List.of(new AdvancedRiskEngine.Claim(1000, 100, 1)))
        );
    }

    @Test
    void killLine31_BoundaryMutant() {
        assertEquals(1000.0, engine.calculatePremium(65, 40, 0, List.of()));
    }

    @Test
    void killLine44_MultiplicationMutant() {
        assertEquals(1000.5, engine.calculatePremium(30, 5, 0,
                List.of(new AdvancedRiskEngine.Claim(100, 50, 0))));
    }
}