import java.util.Arrays;

public class Task3 {
    static void selectionSort(int[] arr){
        int n= arr.length;
        for (int i=n-1;i>=1;i--){
            int maxIndex=0;
            for (int j=1; j<=i;j++){
                if (arr[j]>=arr[maxIndex]){
                    maxIndex=j;
                }
            }
            int temp = arr[i];
            arr[i] = arr[maxIndex];
            arr[maxIndex] = temp;
        }
    }

    public static void main(String[] args) {
        int[] nums = {2, 5, 3, 8, 6};
        System.out.println("Unsorted array: "+ Arrays.toString(nums));
        selectionSort(nums);
        System.out.println("Sorted array: "+ Arrays.toString(nums));

    }
}