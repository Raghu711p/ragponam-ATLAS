class Node{

    int value;
    Node left;
    Node right;
    public Node(int value){
        this.value = value;
        this.left=null;
        this.right=null;
    }
}
class Tree{
    Node root;
    public Tree(){
        root=null;
    }
    public void insert(int value){
       root = insertRec(root, value);
        }
        private Node insertRec(Node root, int value){
        if (root==null){
            root=new Node(value);
            return root;
        }
        if (value< root.value){
            root.left = insertRec(root.left, value);

        }
        else if (value> root.value){
            root.right = insertRec(root.right, value);

        }
        return root;
        }
    public void inorder() {
        inorderRec(root);
        System.out.println();
    }

    private void inorderRec(Node root) {
        if (root != null) {
            inorderRec(root.left);
            System.out.print(root.value + " ");
            inorderRec(root.right);
        }
    }

    }
    public class Task1to4 {
        public static void main(String[] args) {
            Tree tree = new Tree();
            tree.insert(3);
            tree.insert(3);
            tree.insert(5);
            tree.insert(2);
            tree.insert(11);
            tree.insert(6);
            tree.insert(20);
            tree.insert(19);
            System.out.println("In-order traversal: ");

            tree.inorder();
        }
}
