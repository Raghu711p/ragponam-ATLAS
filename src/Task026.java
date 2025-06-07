public class Task026 {
    // main function
    public static void main(String args[])
    {
        // declaring and initializing 2D array
        int arr[][] = { { 2, 7, 9 }, { 3, 6, 1 }, { 7, 4, 2 } };

        // printing 2D array
        for (int i = 0; i < 3; i++) { // rows
            for (int j = 0; j < 3; j++) // columns
                System.out.print(arr[i][j] + " ");

            System.out.println();
        }
    }
}
