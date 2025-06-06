import java.util.Scanner;

public class Task010 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the first number x: ");
        int x = scanner.nextInt();
        System.out.print("Enter the second number y: ");
        int y = scanner.nextInt();
        System.out.print("Enter the third number z: ");
        int z = scanner.nextInt();

        if (x>y && x>z){
            System.out.println(x+ " is greater.");
        } else if (y>z && y>x ) {
            System.out.println(y+ " is greater.");
        }
        else {
            System.out.println(z + " is greater.");
        }
        scanner.close();
        }
    }

