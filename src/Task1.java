class Node {
    int data;
    Node next;

    public Node(int data) {
        this.data = data;
        this.next = null;
    }
}

class LinkedList {
    Node head;

    public LinkedList() {
        this.head = null;
    }

    public void append(int data) {
        Node newNode = new Node(data);
        if (this.head == null) {
            this.head = newNode;
            return;
        }

        Node lastNode = this.head;
        while (lastNode.next != null) {
            lastNode = lastNode.next;
        }
        lastNode.next = newNode;
    }

    public void traverse() {
        Node current = this.head;
        if (current == null) {
            System.out.println("The list is empty.");
            return;
        }

        System.out.print("Linked List Elements: ");
        while (current != null) {
            System.out.print(current.data + " -> ");
            current = current.next;
        }
        System.out.println("null");
    }

    public static void main(String[] args) {
        LinkedList myList = new LinkedList();
        System.out.println("Initial list:");
        myList.traverse();

        myList.append(10);
        myList.append(20);
        myList.append(30);
        System.out.println("\nAfter appending 10, 20, 30:");
        myList.traverse();

        myList.append(5);
        System.out.println("\nAfter appending 5:");
        myList.traverse();

        System.out.println("\nFinal list after operations:");
        myList.traverse();
    }
}
