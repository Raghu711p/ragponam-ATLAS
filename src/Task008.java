class Customer{
        void accept(){
            System.out.println("Accept customer called");
        }
        void display(){
            System.out.println("Display customer called");
        }
    }
    public class Task008 {
        public static void main(String[] args) {
            Customer cObj = new Customer();
            cObj.accept();
            cObj.display();
        }
}

