import java.util.Scanner;

public class Task009 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the first number x: ");
        int x = scanner.nextInt();
        System.out.print("Enter the second number y: ");
        int y = scanner.nextInt();
        if (x>y){
            System.out.println(x+ " is greater");
        }
        else {
            System.out.println(y +" is greater.");
        }

    }
}