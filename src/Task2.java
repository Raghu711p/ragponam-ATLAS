class Node {
    int data;
    Node next;

    public Node(int data) {
        this.data = data;
        this.next = null; // In a circular list, this will be updated
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
            newNode.next = this.head; // For a single node, it points to itself
            return;
        }

        Node current = this.head;
        // Traverse to the last node (the one whose next points to head)
        while (current.next != this.head) {
            current = current.next;
        }

        current.next = newNode;    // The old last node now points to the new node
        newNode.next = this.head;  // The new node points back to the head
    }

    public void traverse() {
        if (this.head == null) {
            System.out.println("The list is empty.");
            return;
        }

        Node current = this.head;
        System.out.print("Circular Linked List Elements: ");
        do {
            System.out.print(current.data + " -> ");
            current = current.next;
        } while (current != this.head); // Continue until we loop back to the head
        System.out.println(" (back to head)"); // Indicate the circular nature
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
