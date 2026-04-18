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
    @DisplayName("C1 — Baseline: nici un risc, nici o daună, nici o loialitate")
    void c1_baseline() {
        double result = engine.calculatePremium(40, 5, 0, List.of());
        assertEquals(1000.0, result, 0.001);
    }

    @Test
    @DisplayName("C2 — D1=T: vârstă < 18 → N1 → EX (throws)")
    void c2_d1_varstaInvalida() {
        
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(17, 0, 0, List.of()));
    }

    @Test
    @DisplayName("C3 — D2=T: experiență > driverAge-18 → N2 → EX (throws)")
    void c3_d2_experientaInvalida() {
        
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(30, 13, 0, List.of()));
    }

    @Test
    @DisplayName("C4 — D3=T: loyaltyYears > 10 → N3 → EX (throws)")
    void c4_d3_loialitateInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(30, 5, 11, List.of()));
    }

    @Test
    @DisplayName("C5 — D4=T: claims null → N4 → EX (throws)")
    void c5_d4_claimsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(30, 5, 0, null));
    }

    @Test
    @DisplayName("C6 — D5=T: tânăr+exp mică → N6→N7, riskMult=1.50")
    void c6_d5_highRisk() {
        double result = engine.calculatePremium(22, 1, 0, List.of());
        assertEquals(1500.0, result, 0.001);
    }

    @Test
    @DisplayName("C7 — D6=T: senior (>65) → N8→N9, riskMult=1.20")
    void c7_d6_senior() {
        double result = engine.calculatePremium(70, 5, 0, List.of());
        assertEquals(1200.0, result, 0.001);
    }

    @Test
    @DisplayName("C8 — D7=T, D8=F: for body cu 1 daună validă → penalty=0.01")
    void c8_d7_forBody_daunaValida() {
        var claim = new AdvancedRiskEngine.Claim(1000, 100, 0);
        double result = engine.calculatePremium(40, 5, 0, List.of(claim));
        assertEquals(1010.0, result, 0.001);
    }

    @Test
    @DisplayName("C9 — D8=T: daună cu faultPercentage=-1 → N12 → EX (throws)")
    void c9_d8_daunaInvalida() {
        var daunaRea = new AdvancedRiskEngine.Claim(0, -1, 0);
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculatePremium(40, 5, 0, List.of(daunaRea)));
    }

    @Test
    @DisplayName("C10 — D9=T, D10=T: while + isEmpty → discount 5%/an, premium=950.0")
    void c10_d9_d10_whileEmpty() {
        double result = engine.calculatePremium(40, 5, 1, List.of());
        assertEquals(950.0, result, 0.001);
    }

    @Test
    @DisplayName("C11 — D11=T: 1 daună veche (yearsAgo=3>2) → discount 2%, premium=980.0")
    void c11_d11_daunaVeche() {
        var daunaVeche = new AdvancedRiskEngine.Claim(0, 0, 3);
        double result = engine.calculatePremium(40, 5, 1, List.of(daunaVeche));
        assertEquals(980.0, result, 0.001);
    }

    @Test
    @DisplayName("C12 — D11=F: 2 daune → while fără discount, premium=1000.0")
    void c12_d11_fals_faraDDiscount() {
        var c1 = new AdvancedRiskEngine.Claim(0, 0, 1);
        var c2 = new AdvancedRiskEngine.Claim(0, 0, 1);
        double result = engine.calculatePremium(40, 5, 1, List.of(c1, c2));
        assertEquals(1000.0, result, 0.001);
    }

    @Test
    @DisplayName("C13 — Back-edge for: 2 daune valide → for rulează de 2×")
    void c13_backEdgeFor_doiClaimsValizi() {
        var c1 = new AdvancedRiskEngine.Claim(0, 100, 5); 
        var c2 = new AdvancedRiskEngine.Claim(0, 100, 5);
        double result = engine.calculatePremium(40, 5, 0, List.of(c1, c2));
        assertEquals(1000.0, result, 0.001);
    }

    @Test
    @DisplayName("C14 — Back-edge while: loyalty=2 → while rulează de 2×, premium=900.0")
    void c14_backEdgeWhile_douaIteratii() {
        double result = engine.calculatePremium(40, 5, 2, List.of());
        assertEquals(900.0, result, 0.001);
    }

    @Test
    @DisplayName("BVA Kill Mutant Linia 29: Exact la limita de high risk (vârstă=25, exp=2)")
    void killMutant_L29_boundaryHighRisk() {
        double result = engine.calculatePremium(25, 2, 0, List.of());
        assertEquals(1500.0, result, 0.001);
    }
    @Test
    @DisplayName("BVA Kill Mutant Linia 56: Exact la limita de vechime dauna (yearsAgo=2)")
    void killMutant_L56_boundaryYearsAgo() {var daunaFixDoiAni = new AdvancedRiskEngine.Claim(0, 0, 2);
        double result = engine.calculatePremium(40, 5, 1, List.of(daunaFixDoiAni));
        assertEquals(1000.0, result, 0.001);
    }
}
