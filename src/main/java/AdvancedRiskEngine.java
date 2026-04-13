import java.util.List;

public class AdvancedRiskEngine {

    public static class Claim {
        public double amount;
        public int faultPercentage; // 0 - 100%
        public int yearsAgo;        // 0 - 5 ani

        public Claim(double amount, int faultPercentage, int yearsAgo) {
            this.amount = amount;
            this.faultPercentage = faultPercentage;
            this.yearsAgo = yearsAgo;
        }
    }

    public double calculatePremium(int driverAge, int experienceYears, int loyaltyYears, List<Claim> claims) {

        if (driverAge < 18 || driverAge > 99) throw new IllegalArgumentException("Vârstă invalidă");
        if (experienceYears < 0 || experienceYears > (driverAge - 18)) throw new IllegalArgumentException("Experiență invalidă");
        if (loyaltyYears < 0 || loyaltyYears > 10) throw new IllegalArgumentException("Ani loialitate în afara limitelor [0, 10]");
        if (claims == null) throw new IllegalArgumentException("Istoricul daunelor nu poate fi null");

        double basePremium = 1000.0;
        double riskMultiplier = 1.0;

        if ((driverAge <= 25 && experienceYears <= 2) || claims.size() >= 3) {
            riskMultiplier += 0.50;
        } else if (driverAge > 65) {
            riskMultiplier += 0.20;
        }

        double claimsPenalty = 0.0;
        for (int i = 0; i < claims.size(); i++) {
            Claim c = claims.get(i);
            if (c.faultPercentage < 0 || c.faultPercentage > 100 || c.yearsAgo < 0 || c.amount < 0) {
                throw new IllegalArgumentException("Date daună invalide");
            }

            double timeDecayCurve = Math.max(0, 5 - c.yearsAgo) / 5.0;

            double severityPoints = (c.amount * (c.faultPercentage / 100.0)) / 1000.0;
            claimsPenalty += (severityPoints * timeDecayCurve * 0.01);
        }
        riskMultiplier += claimsPenalty;

        double loyaltyDiscount = 0.0;
        int maxDiscountYears = Math.min(loyaltyYears, 5); // Plafonăm la max 5 ani de reducere

        int i = 1;
        while (i <= maxDiscountYears) {
            if (claims.isEmpty()) {
                loyaltyDiscount += 0.05;
            } else if (claims.size() == 1 && claims.get(0).yearsAgo > 2) {
                loyaltyDiscount += 0.02;
            }

            i++;
        }

        // if (loyaltyDiscount > 0.25) loyaltyDiscount = 0.25;

        double finalPremium = basePremium * (riskMultiplier - loyaltyDiscount);
        return Math.max(finalPremium, 500.0);
    }
}