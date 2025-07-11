import java.util.Arrays;

public class Task9 {
    static void insertionSort(int[] arr){
        int n = arr.length;
        for (int i = 1; i < n; i++){
            int key = arr[i];
            int j = i - 1;

            while (j >= 0 && arr[j] > key){
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    public static void main(String[] args) {
        int[] nums = {2, 4, 8, 5, 9, 10};
        System.out.println("Unsorted array: " + Arrays.toString(nums));
        insertionSort(nums);
        System.out.println("Sorted using insertion sort: " + Arrays.toString(nums));
    }
}