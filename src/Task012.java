import java.util.Objects;
import java.util.Scanner;

public class Task012 {

    private static final String CORRECT_LOGIN_ID = "raghu";
    private static final String CORRECT_PASSWORD = "password";

    public static void validateWithWhileLoop(Scanner scanner) {
        System.out.println("--- Starting Login Validation with WHILE Loop ---");
        String enteredLoginId;
        String enteredPassword;
        int attemptCount = 0;

        enteredLoginId = "";
        enteredPassword = "";

        while (!Objects.equals(enteredLoginId, CORRECT_LOGIN_ID) || !Objects.equals(enteredPassword, CORRECT_PASSWORD)) {
            attemptCount++;

            System.out.println("Attempt #" + attemptCount + ":");
            System.out.print("Enter your Login ID: ");
            enteredLoginId = scanner.nextLine();

            System.out.print("Enter your Password: ");
            enteredPassword = scanner.nextLine();

            if (!Objects.equals(enteredLoginId, CORRECT_LOGIN_ID) || !Objects.equals(enteredPassword, CORRECT_PASSWORD)) {
                System.out.println("Invalid Login ID or Password. Please try again.\n");
            }
        }

        System.out.println("\nLogin successful using WHILE loop!");
        System.out.println("You logged in after " + attemptCount + " attempt(s).\n");
    }

    public static void validateWithDoWhileLoop(Scanner scanner) {
        System.out.println("--- Starting Login Validation with DO-WHILE Loop ---");
        String enteredLoginId;
        String enteredPassword;
        int attemptCount = 0;

        do {
            attemptCount++;

            System.out.println("Attempt #" + attemptCount + ":");
            System.out.print("Enter your Login ID: ");
            enteredLoginId = scanner.nextLine();

            System.out.print("Enter your Password: ");
            enteredPassword = scanner.nextLine();

            if (!Objects.equals(enteredLoginId, CORRECT_LOGIN_ID) || !Objects.equals(enteredPassword, CORRECT_PASSWORD)) {
                System.out.println("Invalid Login ID or Password. Please try again.\n");
            }

        } while (!Objects.equals(enteredLoginId, CORRECT_LOGIN_ID) || !Objects.equals(enteredPassword, CORRECT_PASSWORD));

        System.out.println("Login successful using DO-WHILE loop!");
        System.out.println("You logged in after " + attemptCount + " attempt(s).\n");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        validateWithWhileLoop(scanner);

        validateWithDoWhileLoop(scanner);

        scanner.close();
        System.out.println("Program finished.");
    }
}
