public class Task036 {
    public static void add(int x, float y){
        System.out.println(x+" ||| "+y);
    }
    public static void add(float x, int y){
        System.out.println(x+" }}{{ "+y);
    }

    public static void main(String[] args) {
        add(10.50f, 60);
        add(30,39.66f);
    }
}
