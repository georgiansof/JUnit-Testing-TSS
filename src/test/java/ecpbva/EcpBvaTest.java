package ecpbva;

import engine.AdvancedRiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pachet 1: ECP & BVA - Validări și Calcule de Bază")
class EcpBvaTest {
    private AdvancedRiskEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AdvancedRiskEngine();
    }

    @Nested
    @DisplayName("EPC clase invalide — driverAge")
    class InvalidDriverAge {

        @Test
        @DisplayName("BVA A_inv1: driverAge=17 (frontieră superioară < 18) → excepție")
        void age17_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(17, 0, 0, List.of()));
        }

        @Test
        @DisplayName("BVA A_inv2: driverAge=100 (frontieră inferioară > 99) → excepție")
        void age100_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(100, 0, 0, List.of()));
        }
    }

    @Nested
    @DisplayName("EPC clase invalide — experienceYears")
    class InvalidExperienceYears {

        @Test
        @DisplayName("BVA E_inv1: experienceYears=-1 → excepție")
        void experience_negative1_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, -1, 0, List.of()));
        }

        @Test
        @DisplayName("BVA E_inv2: driverAge=30, experienceYears=13 (max valid=12) → excepție")
        void experience_exceedsMax_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 13, 0, List.of()));
        }
    }

    @Nested
    @DisplayName("EPC clase invalide — loyaltyYears")
    class InvalidLoyaltyYears {

        @Test
        @DisplayName("BVA L_inv1: loyaltyYears=-1 → excepție")
        void loyalty_negative1_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, -1, List.of()));
        }

        @Test
        @DisplayName("BVA L_inv2: loyaltyYears=11 (frontieră > 10) → excepție")
        void loyalty_11_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 11, List.of()));
        }
    }

    @Nested
    @DisplayName("EPC clase invalide — claims și atribute daună")
    class InvalidClaims {

        @Test
        @DisplayName("EPC C_null: claims=null → excepție")
        void claims_null_throwsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 0, null));
        }

        @Test
        @DisplayName("BVA: claim.faultPercentage=-1 (frontieră < 0) → excepție")
        void claim_faultPercentage_negative1_throwsException() {
            var claim = new AdvancedRiskEngine.Claim(1000, -1, 1);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 0, List.of(claim)));
        }

        @Test
        @DisplayName("BVA: claim.faultPercentage=101 (frontieră > 100) → excepție")
        void claim_faultPercentage_101_throwsException() {
            var claim = new AdvancedRiskEngine.Claim(1000, 101, 1);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 0, List.of(claim)));
        }

        @Test
        @DisplayName("BVA: claim.yearsAgo=-1 (frontieră < 0) → excepție")
        void claim_yearsAgo_negative1_throwsException() {
            var claim = new AdvancedRiskEngine.Claim(1000, 50, -1);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 0, List.of(claim)));
        }

        @Test
        @DisplayName("BVA: claim.amount=-1 (frontieră < 0) → excepție")
        void claim_amount_negative_throwsException() {
            var claim = new AdvancedRiskEngine.Claim(-1, 50, 1);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.calculatePremium(30, 5, 0, List.of(claim)));
        }
    }

    @Nested
    @DisplayName("BVA frontiere driverAge — clasa A1 [18, 25]")
    class DriverAgeA1_YoungHighRisk {

        @Test
        @DisplayName("BVA: age=18 (min valid), exp=0 (min E1), loyalty=0, noClaims → HIGH RISK 1500.0")
        void age18_exp0_noLoyalty_noClaims() {
            double result = engine.calculatePremium(18, 0, 0, List.of());
            assertEquals(1500.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA: age=25 (max A1), exp=2 (max E1) → HIGH RISK 1500.0")
        void age25_exp2_noLoyalty_noClaims() {
            double result = engine.calculatePremium(25, 2, 0, List.of());
            assertEquals(1500.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA: age=25, exp=3 (frontieră inferioară E2) → NORMAL 1000.0")
        void age25_exp3_noLoyalty_noClaims() {
            double result = engine.calculatePremium(25, 3, 0, List.of());
            assertEquals(1000.0, result, 0.001);
        }
    }

    @Nested
    @DisplayName("BVA frontiere driverAge — clasa A2 [26, 65]")
    class DriverAgeA2_MidNormalRisk {

        @Test
        @DisplayName("BVA: age=26 (frontieră inferioară A2), exp=0, noClaims → NORMAL 1000.0")
        void age26_exp0_noLoyalty_noClaims() {
            double result = engine.calculatePremium(26, 0, 0, List.of());
            assertEquals(1000.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA: age=65 (frontieră superioară A2), exp=0, noClaims → NORMAL 1000.0")
        void age65_exp0_noLoyalty_noClaims() {
            double result = engine.calculatePremium(65, 0, 0, List.of());
            assertEquals(1000.0, result, 0.001);
        }
    }

    @Nested
    @DisplayName("BVA frontiere driverAge — clasa A3 [66, 99]")
    class DriverAgeA3_SeniorRisk {

        @Test
        @DisplayName("BVA: age=66 (frontieră inferioară A3), noClaims → SENIOR 1200.0")
        void age66_noLoyalty_noClaims() {
            double result = engine.calculatePremium(66, 0, 0, List.of());
            assertEquals(1200.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA: age=99 (max valid), noClaims → SENIOR 1200.0")
        void age99_noLoyalty_noClaims() {
            double result = engine.calculatePremium(99, 0, 0, List.of());
            assertEquals(1200.0, result, 0.001);
        }
    }

    @Nested
    @DisplayName("BVA frontiere loyaltyYears — fără daune")
    class LoyaltyYears_NoClaims {

        @Test
        @DisplayName("BVA L0: loyalty=0 (min valid) → fără discount, premium=1000.0")
        void loyalty0_noClaims() {
            double result = engine.calculatePremium(30, 5, 0, List.of());
            assertEquals(1000.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA L1: loyalty=1 (frontieră inferioară discount) → premium=950.0")
        void loyalty1_noClaims() {
            double result = engine.calculatePremium(30, 5, 1, List.of());
            assertEquals(950.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA L1/L2: loyalty=5 (plafon discount, max 5 ani) → premium=750.0")
        void loyalty5_noClaims() {
            double result = engine.calculatePremium(30, 5, 5, List.of());
            assertEquals(750.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA L2: loyalty=6 (deasupra plafonului) → același discount ca 5, premium=750.0")
        void loyalty6_noClaims() {
            double result = engine.calculatePremium(30, 5, 6, List.of());
            assertEquals(750.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA L2: loyalty=10 (max valid) → plafonat, premium=750.0")
        void loyalty10_noClaims() {
            double result = engine.calculatePremium(30, 5, 10, List.of());
            assertEquals(750.0, result, 0.001);
        }
    }

    @Nested
    @DisplayName("BVA frontiere claims.size()")
    class ClaimsSize {

        @Test
        @DisplayName("BVA C1: 1 daună (amount=1000, fault=100%, yearsAgo=0) → premium=1010.0")
        void oneClaim_fullFault_recentClaim() {
            var claim = new AdvancedRiskEngine.Claim(1000, 100, 0);
            double result = engine.calculatePremium(30, 5, 0, List.of(claim));
            assertEquals(1010.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA C2: 2 daune → fără HIGH RISK, premium=1020.0")
        void twoClaims_noHighRisk() {
            var c1 = new AdvancedRiskEngine.Claim(1000, 100, 0);
            var c2 = new AdvancedRiskEngine.Claim(1000, 100, 0);
            double result = engine.calculatePremium(30, 5, 0, List.of(c1, c2));
            assertEquals(1020.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA C3: 3 daune (frontieră HIGH RISK) → riskMultiplier +0.50, premium=1500.0")
        void threeClaims_highRiskTriggered() {
            var c1 = new AdvancedRiskEngine.Claim(0, 0, 5);
            var c2 = new AdvancedRiskEngine.Claim(0, 0, 5);
            var c3 = new AdvancedRiskEngine.Claim(0, 0, 5);
            double result = engine.calculatePremium(30, 5, 0, List.of(c1, c2, c3));
            assertEquals(1500.0, result, 0.001);
        }
    }

    @Nested
    @DisplayName("BVA frontiere claim.yearsAgo — decay și condiție loyalty")
    class ClaimYearsAgo {

        @Test
        @DisplayName("BVA: yearsAgo=0 (decay=1.0, maxim) → premium=1050.0")
        void yearsAgo0_maxDecay() {
            var claim = new AdvancedRiskEngine.Claim(5000, 100, 0);
            double result = engine.calculatePremium(30, 5, 0, List.of(claim));
            assertEquals(1050.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA: yearsAgo=2 (NU satisface condiția > 2) → fără discount loyalty, premium=1000.0")
        void yearsAgo2_noLoyaltyDiscount() {
            var claim = new AdvancedRiskEngine.Claim(0, 0, 2);
            double result = engine.calculatePremium(30, 5, 5, List.of(claim));
            assertEquals(1000.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA: yearsAgo=3 (satisface condiția > 2) → discount loyalty activ, premium=900.0")
        void yearsAgo3_loyaltyDiscountApplies() {
            var claim = new AdvancedRiskEngine.Claim(0, 0, 3);
            double result = engine.calculatePremium(30, 5, 5, List.of(claim));
            assertEquals(900.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA: yearsAgo=5 (decay=0, dauna nu mai contează) → premium=1000.0")
        void yearsAgo5_zeroDecay_noPenalty() {
            var claim = new AdvancedRiskEngine.Claim(9999, 100, 5);
            double result = engine.calculatePremium(30, 5, 0, List.of(claim));
            assertEquals(1000.0, result, 0.001);
        }
    }

    @Nested
    @DisplayName("BVA frontiere claim.faultPercentage")
    class ClaimFaultPercentage {

        @Test
        @DisplayName("BVA: faultPercentage=0 (frontieră inferioară validă) → nicio penalizare, premium=1000.0")
        void faultPercentage0_noSeverity() {
            var claim = new AdvancedRiskEngine.Claim(100_000, 0, 0);
            double result = engine.calculatePremium(30, 5, 0, List.of(claim));
            assertEquals(1000.0, result, 0.001);
        }

        @Test
        @DisplayName("BVA: faultPercentage=100 (frontieră superioară validă) → penalizare maximă per daună, premium=1010.0")
        void faultPercentage100_fullSeverity() {
            var claim = new AdvancedRiskEngine.Claim(1000, 100, 0);
            double result = engine.calculatePremium(30, 5, 0, List.of(claim));
            assertEquals(1010.0, result, 0.001);
        }
    }

    @Test
    @DisplayName("EPC: podeaua de 500 — prima finală nu coboară niciodată sub 500.0")
    void premiumFloor_neverBelowMinimum() {
        double result = engine.calculatePremium(40, 5, 10, List.of());
        assertTrue(result >= 500.0, "Prima finală trebuie să fie cel puțin 500.0");
        assertEquals(750.0, result, 0.001);
    }
}