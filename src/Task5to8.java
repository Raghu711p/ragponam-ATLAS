import java.util.Stack;

public class Task5to8 {
    public static void main(String[] args) {
        Stack stack = new Stack<>();
        stack.push("Raghu");
        stack.push(5);
        stack.push("Ram");
        stack.push("mouse");
        stack.push("jerry");
        stack.push("cat");


        System.out.println("Initial stack: "+stack);
        System.out.println("Searching element "+"Raghu: "+stack.search("Raghu"));
        System.out.println("Searching element '5': "+stack.search(5));
        System.out.println("Peeking the stack: "+stack.peek());
        System.out.println("Checking if stack is empty:"+stack.isEmpty());
        stack.pop();

        System.out.println("Printing after a pop operation: "+stack);

    }
}
