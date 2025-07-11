import java.util.Arrays;

public class Task6 {
    static void bubbleSort(int[] arr){
        int n=arr.length;
        for (int i=n-1;i>=1;i--){
            for (int j=1; j<=i;j++){
                if (arr[j-1]>arr[j]){
                    int temp = arr[j];
                    arr[j] = arr[j-1];
                    arr[j-1] = temp;
                }
            }
        }
    }

    public static void main(String[] args) {
        int[] nums = {2, 4, 8, 5, 9, 10};
        System.out.println("Unsorted array: "+Arrays.toString(nums));
        bubbleSort(nums);
        System.out.println("Sorted array using bubble sort: "+ Arrays.toString(nums));
    }
}
