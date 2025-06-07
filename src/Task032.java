class Calculation2 {
    int z;

    public void addition(int x, int y) {
        z = x + y;
        System.out.println("The sum of the given numbers:" + z);
    }

    public void Subtraction(int x, int y) {
        z = x - y;
        System.out.println("The difference between the given numbers:" + z);
    }
}

public class Task032 extends Calculation {

    public void division(int x, int y) {
        if (y != 0) {
            z = x / y;
            System.out.println("The quotient of the given numbers:" + z);
        } else {
            System.out.println("Error: Cannot divide by zero!");
        }
    }

    public static void main(String args[]) {
        int a = 30, b = 5;

        Task032 calculatorDemo = new Task032();

        calculatorDemo.addition(a, b);
        calculatorDemo.Subtraction(a, b);

        calculatorDemo.division(a, b);

        calculatorDemo.division(a, 0);
    }
}
