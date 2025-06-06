import java.util.Scanner;

public class Task003 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the first number: ");
        int x = scanner.nextInt();
        System.out.println("Enter the second number: ");
        int y = scanner.nextInt();
        int result = x+y;
        System.out.println("Sum of two numbers is: "+ result);
    }
}
