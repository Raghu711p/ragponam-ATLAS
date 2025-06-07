public class Task035{
    public static void add(int x, int y){
        System.out.println(x + " %%%% "+ y);
    }
    public static void add(char x, char y){
        System.out.println(x + " ###### "+ y);
    }
    public static void main(String[] args){
        add(10,50);
        add('R', 'R');
    }
}