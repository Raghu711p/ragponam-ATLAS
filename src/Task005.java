public class Task005 {
    static int a =8;
    static int b=4;
    static void  add(){
     int sum = a+b;
        System.out.println(sum);
    }
    static void diff(){
        int diff = a-b;
        System.out.println(diff);
    }
    static void prod(){
        int prod = a*b;
        System.out.println(prod);
    }
    static void div(){
        int div = a/b;
        System.out.println(div);
    }

    public static void main(String[] args) {
        add();
        diff();
        prod();
        div();

    }
}
