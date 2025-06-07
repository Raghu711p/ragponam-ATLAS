public class Task034{
   public static void add(int x, int y){
        System.out.println(x+ " &&& "+ y);
    }
    public static void add(int x, int y, int z ){
        System.out.println(x + " &&& "+ y+ " &&& "+ z);
    }
    public static void main(String[] args){
        Task034 tobj = new Task034();
        add(10,20);
        add(100,200,300);
    }
}