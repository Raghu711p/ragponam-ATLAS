class TreeNode {
    int item;
    TreeNode left, right;

    TreeNode(int item) {
        this.item = item;
        left = right = null;
    }
}

class BinarySearchTreeOp02 {
    TreeNode root;

    public BinarySearchTreeOp02() {
        this.root = null;
    }

    public void insert(int item) {
        root = insertRec(root, item);
    }

    private TreeNode insertRec(TreeNode root, int item) {
        if (root == null) {
            root = new TreeNode(item);
            return root;
        }

        if (item < root.item) {
            root.left = insertRec(root.left, item);
        } else if (item > root.item) {
            root.right = insertRec(root.right, item);
        }

        return root;
    }

    public TreeNode search(int key) {
        TreeNode current = root;
        while (current != null) {
            if (key == current.item) {
                return current;
            } else if (key < current.item) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    public void inorder() {
        inorderRec(root);
        System.out.println();
    }

    private void inorderRec(TreeNode root) {
        if (root != null) {
            inorderRec(root.left);
            System.out.print(root.item + " ");
            inorderRec(root.right);
        }
    }

    public static void main(String[] args) {
        BinarySearchTreeOp02 bst = new BinarySearchTreeOp02();

        System.out.println("Inserting elements: 50, 30, 70, 20, 40, 60, 80");
        bst.insert(50);
        bst.insert(30);
        bst.insert(70);
        bst.insert(20);
        bst.insert(40);
        bst.insert(60);
        bst.insert(80);

        System.out.print("In-order traversal: ");
        bst.inorder();

        System.out.println("\nSearching for elements:");
        int searchKey1 = 40;
        TreeNode result1 = bst.search(searchKey1);
        if (result1 != null) {
            System.out.println("Found " + searchKey1 + " in the tree.");
        } else {
            System.out.println(searchKey1 + " not found in the tree.");
        }

        int searchKey2 = 99;
        TreeNode result2 = bst.search(searchKey2);
        if (result2 != null) {
            System.out.println("Found " + searchKey2 + " in the tree.");
        } else {
            System.out.println(searchKey2 + " not found in the tree.");
        }
    }
}
