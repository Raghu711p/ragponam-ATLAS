
    class Employee{
        protected int salary;
        public int emp_id;
        private int pwd;
    }
    class hr extends employee{
        void details() {
//            super.pwd = 1234;
            super.salary = 50000;
            super.emp_id = 12345;
            System.out.println(salary+" "+emp_id);
        }

    }
    public class Task037 {
        public static void main(String[] args) {
            hr objhr = new hr();
            objhr.details();


        }
}

