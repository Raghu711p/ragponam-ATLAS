public class Task039 {

    public static void main(String [] args) {

        FullTimeEmployee e = new FullTimeEmployee("George W.", "Houston, TX", 43, 50000.0);

        System.out.println("\nCall mailCheck using Employee reference--");
        e.mailCheck();

        System.out.println("\nCompute Pay for FullTimeEmployee:");
        System.out.println("Pay: $" + e.computePay());
    }
}

abstract class Employee {
    private String name;
    private String address;
    private int number;

    public Employee(String name, String address, int number) {
        System.out.println("Constructing an Employee");
        this.name = name;
        this.address = address;
        this.number = number;
    }

    public double computePay() {
        System.out.println("Inside Employee computePay (default calculation)");
        return 0.0;
    }

    public void mailCheck() {
        System.out.println("Mailing a check to " + this.name + " " + this.address);
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Address: " + address + ", Number: " + number;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String newAddress) {
        address = newAddress;
    }

    public int getNumber() {
        return number;
    }
}

class FullTimeEmployee extends Employee {
    private double monthlySalary;

    public FullTimeEmployee(String name, String address, int number, double monthlySalary) {
        super(name, address, number);
        this.monthlySalary = monthlySalary;
        System.out.println("Constructing a FullTimeEmployee");
    }

    @Override
    public double computePay() {
        System.out.println("Inside FullTimeEmployee computePay");
        return monthlySalary;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(double monthlySalary) {
        this.monthlySalary = monthlySalary;
    }
}
