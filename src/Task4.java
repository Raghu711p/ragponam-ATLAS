//class Node {
//    int data;
//    Node next;
//
//    public Node(int data) {
//        this.data = data;
//        this.next = null;
//    }
//}
//
//class LinkedList {
//    Node head;
//
//    public LinkedList() {
//        this.head = null;
//    }
//
//    public void append(int data) {
//        Node newNode = new Node(data);
//        if (this.head == null) {
//            this.head = newNode;
//            newNode.next = this.head;
//            return;
//        }
//
//        Node current = this.head;
//        while (current.next != this.head) {
//            current = current.next;
//        }
//
//        current.next = newNode;
//        newNode.next = this.head;
//    }
//
//    public void deleteNode(int key) {
//        if (this.head == null) {
//            System.out.println("List is empty. Cannot delete.");
//            return;
//        }
//
//        Node current = this.head;
//        Node prev = null;
//
//        // Case 1: Deleting the only node in the list
//        if (current.data == key && current.next == this.head) {
//            this.head = null;
//            return;
//        }
//
//        // Case 2: Deleting the head node (when there are multiple nodes)
//        if (current.data == key) {
//            // Find the last node
//            while (current.next != this.head) {
//                current = current.next;
//            }
//            current.next = this.head.next;
//            this.head = this.head.next;
//            return;
//        }
//
//        // Case 3: Deleting a node other than the head
//        prev = current;
//        current = current.next;
//        while (current != this.head && current.data != key) {
//            prev = current;
//            current = current.next;
//        }
//
//        // If the node was found (and it's not the head, which was handled above)
//        if (current != this.head) { // Ensure we didn't loop back to head without finding it
//            prev.next = current.next;
//        } else {
//            System.out.println("Node with data '" + key + "' not found.");
//        }
//    }
//
//    public void traverse() {
//        if (this.head == null) {
//            System.out.println("The list is empty.");
//            return;
//        }
//
//        Node current = this.head;
//        System.out.print("Circular Linked List Elements: ");
//        do {
//            System.out.print(current.data + " -> ");
//            current = current.next;
//        } while (current != this.head);
//        System.out.println(" (back to head)");
//    }
//
//    public static void main(String[] args) {
//        LinkedList myList = new LinkedList();
//        System.out.println("Initial list:");
//        myList.traverse();
//
//        myList.append(10);
//        myList.append(20);
//        myList.append(30);
//        System.out.println("\nAfter appending 10, 20, 30:");
//        myList.traverse();
//
//        myList.append(5);
//        System.out.println("\nAfter appending 5:");
//        myList.traverse();
//
//        System.out.println("\n--- Deletion Examples ---");
//
//        myList.deleteNode(20);
//        System.out.println("\nAfter deleting 20:");
//        myList.traverse();
//
//        myList.deleteNode(10);
//        System.out.println("\nAfter deleting 10 (old head):");
//        myList.traverse();
//
//        myList.deleteNode(5);
//        System.out.println("\nAfter deleting 5:");
//        myList.traverse();
//
//        myList.deleteNode(30);
//        System.out.println("\nAfter deleting 30 (last node):");
//        myList.traverse();
//
//        myList.deleteNode(100);
//        System.out.println("\nAfter trying to delete 100 from an empty list:");
//        myList.traverse();
//
//        myList.append(100);
//        System.out.println("\nAfter appending 100:");
//        myList.traverse();
//        myList.deleteNode(100);
//        System.out.println("\nAfter deleting 100 (single node):");
//        myList.traverse();
//    }
//}
