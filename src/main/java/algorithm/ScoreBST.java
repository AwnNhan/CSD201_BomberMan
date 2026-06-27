/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package algorithm;

/**
 *
 * @author Admin
 */
public class ScoreBST {

    public class Node {

        String name;
        int score;
        Node left, right;

        public Node(String name, int score) {
            this.name = name;
            this.score = score;
            this.left = this.right = null;
        }
    }

    private Node root;

    public ScoreBST() {
        root = null;
    }

    public void insertScore(String name, int score) {
        root = insertRec(root, name, score);
    }

    private Node insertRec(Node root, String name, int score) {
        if (root == null) {
            root = new Node(name, score);
            return root;
        }
        if (score <= root.score) {
            root.left = insertRec(root.left, name, score);
        } else if (score > root.score) {
            root.right = insertRec(root.right, name, score);
        }
        return root;
    }

    // Duyệt In-order giảm dần (Right -> Root -> Left) để xếp hạng
    public void printInOrderDescending() {
        inOrderRec(root);
    }

    private void inOrderRec(Node root) {
        if (root != null) {
            inOrderRec(root.right);
            System.out.println(root.name + " - Score: " + root.score);
            inOrderRec(root.left);
        }
    }

    public String getLeaderboard() {
        StringBuilder sb = new StringBuilder();
        buildLeaderboard(root, sb);
        return sb.toString();
    }

    private void buildLeaderboard(Node root, StringBuilder sb) {
        if (root != null) {
            buildLeaderboard(root.right, sb);
            sb.append(root.name)
                    .append(" - ")
                    .append(root.score)
                    .append(" pts\n");
            buildLeaderboard(root.left, sb);
        }
    }
}