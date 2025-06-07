class Customer{
    int cost = 40;
    String items = "Tomatoes";
    Customer(){
        System.out.println("Constructor called");
    }
    void purchage_list(){
        System.out.println("cost of tomatoes in Customer class is "+ cost);
    }
}
public class Task033 extends Customer {

    void billing(){
        String items = "onions";
        int cost = 30;

        super.items = "Potatoes";
        super.cost = 50;
        super.purchage_list();

        System.out.println(items);
        System.out.println(cost);
        System.out.println("***************************");
        System.out.println(super.items);
        System.out.println(super.cost);
        // return 0;
    }
    public static void main(String[] args){
        Customer cobj =new Customer();
        cobj.purchage_list();
        Task033 tobj = new Task033();
        tobj.billing();

    }

}