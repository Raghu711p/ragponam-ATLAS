    import java.util.Arrays;

    public class Task14 {

        static void mergeSort(int[] arr, int left, int right) {
            if (left < right) {
                int mid = left + (right - left) / 2;

                mergeSort(arr, left, mid);
                mergeSort(arr, mid + 1, right);

                merge(arr, left, mid, right);
            }
        }

         static void merge(int[] arr, int left, int mid, int right) {
            int num1 = mid - left + 1;
            int num2 = right - mid;

            int[] L = new int[num1];
            int[] R = new int[num2];

            for (int i = 0; i < num1; ++i) {
                L[i] = arr[left + i];
            }
            for (int j = 0; j < num2; ++j) {
                R[j] = arr[mid + 1 + j];
            }

            int i = 0;
            int j = 0;
            int k = left;

            while (i < num1 && j < num2) {
                if (L[i] <= R[j]) {
                    arr[k] = L[i];
                    i++;
                } else {
                    arr[k] = R[j];
                    j++;
                }
                k++;
            }

            while (i < num1) {
                arr[k] = L[i];
                i++;
                k++;
            }

            while (j < num2) {
                arr[k] = R[j];
                j++;
                k++;
            }
        }

        public static void main(String[] args) {
            int[] nums = {2, 6, 7, 5, 8, 9, 1, 2, 11, 10, 19};
            System.out.println("Unsorted array: " + Arrays.toString(nums));
            mergeSort(nums, 0, nums.length-1);
            System.out.println("Sorted Array: "+ Arrays.toString(nums));
        }
    }