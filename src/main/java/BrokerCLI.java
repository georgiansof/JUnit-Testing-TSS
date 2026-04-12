import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class BrokerCLI {

    static class Client {
        String name;
        int age;
        int experience;
        int loyaltyYears;
        List<AdvancedRiskEngine.Claim> claims;

        public Client(String name, int age, int experience, int loyaltyYears, List<AdvancedRiskEngine.Claim> claims) {
            this.name = name;
            this.age = age;
            this.experience = experience;
            this.loyaltyYears = loyaltyYears;
            this.claims = claims;
        }
    }

    public static void main(String[] args) {
        AdvancedRiskEngine engine = new AdvancedRiskEngine();
        Scanner scanner = new Scanner(System.in);

        List<Client> database = new ArrayList<>();

        database.add(new Client("Andrei (Student)", 20, 1, 0, new ArrayList<>()));

        database.add(new Client("Maria (Client Premium)", 45, 20, 5, new ArrayList<>()));

        List<AdvancedRiskEngine.Claim> dauneIon = Arrays.asList(
                new AdvancedRiskEngine.Claim(50000.0, 100, 1) // 50.000 RON daună, 100% culpa lui, acum 1 an
        );
        database.add(new Client("Ion (Risc Istoric)", 35, 10, 2, dauneIon));

        System.out.println("==================================================");
        System.out.println("   TERMINAL BROKER ASIGURĂRI (RISK ENGINE v2.0)   ");
        System.out.println("==================================================");

        while (true) {
            System.out.println("\nClienți disponibili în sistem:");
            for (int i = 0; i < database.size(); i++) {
                System.out.println("[" + (i + 1) + "] " + database.get(i).name);
            }
            System.out.println("[0] Ieșire din aplicație");
            System.out.print("\nSelectează ID-ul clientului pentru ofertare: ");

            int choice = scanner.nextInt();

            if (choice == 0) {
                System.out.println("Închidere sistem...");
                break;
            }

            if (choice > 0 && choice <= database.size()) {
                Client c = database.get(choice - 1);
                processClient(engine, c);
            } else {
                System.out.println("Opțiune invalidă. Încearcă din nou.");
            }
        }
        scanner.close();
    }

    private static void processClient(AdvancedRiskEngine engine, Client c) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("ANALIZĂ DE RISC PENTRU: " + c.name.toUpperCase());
        System.out.println("Date: " + c.age + " ani | " + c.experience + " ani exp. | " + c.loyaltyYears + " ani loialitate | " + c.claims.size() + " daune înregistrate.");
        System.out.println("--------------------------------------------------");

        try {
            double finalPremium = engine.calculatePremium(c.age, c.experience, c.loyaltyYears, c.claims);

            System.out.println(">> PRIMA DE BAZĂ: 1000.00 RON");

            if (c.age <= 25 && c.experience <= 2) {
                System.out.println("   ! Penalizare vârstă/experiență: Risc ridicat (+50%)");
            } else if (c.age > 65) {
                System.out.println("   ! Penalizare senioritate: Reflexe reduse (+20%)");
            }

            if (!c.claims.isEmpty()) {
                System.out.println("   ! Penalizare istoric daune: Daune recente atrag multiplicatori de risc.");
            }

            if (c.loyaltyYears > 0 && c.claims.isEmpty()) {
                System.out.println("   * Bonus loialitate aplicat (" + c.loyaltyYears + " ani x 5% reducere)");
            }

            System.out.println(">> COST FINAL OFERTAT: " + finalPremium + " RON");

        } catch (IllegalArgumentException e) {
            System.out.println(">> EROARE DE PROCESARE: " + e.getMessage());
        }
        System.out.println("--------------------------------------------------\n");
    }
}