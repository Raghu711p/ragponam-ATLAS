class Student {
    public int roll_no;
    public String name;

    Student(int Roll_no, String Name) {
        this.roll_no = Roll_no;
        this.name = Name;
        System.out.println("Student object created: " + this.name + " (" + this.roll_no + ")");
    }
}

public class Task023 {
    public static void main(String[] args) {

        Student[] arr;

        arr = new Student[5];

        System.out.println("Initializing Student array:");
        arr[0] = new Student(1, "aman");
        arr[1] = new Student(2, "vaibhav");
        arr[2] = new Student(3, "shikar");
        arr[3] = new Student(4, "dharmesh");
        arr[4] = new Student(5, "mohit");

        System.out.println("\nAccessing the elements of the specified array:");
        for (int i = 0; i < arr.length; i++) {
            System.out.println("Element at " + i + " : { "
                    + arr[i].roll_no + " "
                    + arr[i].name + " }");
        }
    }
}
