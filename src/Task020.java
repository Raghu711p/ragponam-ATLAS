public class Task020 {

    public static void main(String[] args) {
        char[] name = {'R', 'a', 'g', 'h', 'u'};

        // Corrected line: Convert the char array to a String before printing.
        System.out.println("My name as a character array: " + new String(name));

        int n = name.length;

        System.out.println("There are " + n + " letters in my name.");

        System.out.print("\nEach letter in my name: ");
        for (int i = 0; i < n; i++) {
            System.out.print(name[i] + " ");
        }
        System.out.println();
    }
}
