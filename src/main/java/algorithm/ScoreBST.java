/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
    private final String FILE_PATH = "highscore.txt"; // Tên file lưu trữ

    public ScoreBST() {
        root = null;
        loadFromFile(); // Tự động load dữ liệu từ file khi khởi động game
    }

    public void insertScore(String name, int score) {
        root = insertRec(root, name, score);
        saveToFile(); // Tự động lưu lại vào file mỗi khi có điểm mới
    }

    private Node insertRec(Node root, String name, int score) {
        if (root == null) {
            root = new Node(name, score);
            return root;
        }
        if (score <= root.score) {
            root.left = insertRec(root.left, name, score);
        } else {
            root.right = insertRec(root.right, name, score);
        }
        return root;
    }

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

    // ==========================================
    // HÀM ĐỌC DỮ LIỆU TỪ FILE TXT
    // ==========================================
    private void loadFromFile() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                file.createNewFile(); 
                return;
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    // Gọi hàm nội bộ để đưa vào cây (không gọi insertScore để tránh ghi đè file liên tục lúc đọc)
                    root = insertRec(root, name, score);
                }
            }
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.out.println("Lỗi đọc file điểm số: " + e.getMessage());
        }
    }

    // ==========================================
    // HÀM GHI DỮ LIỆU XUỐNG FILE TXT
    // ==========================================
    private void saveToFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false)); 
            saveTreeToFile(root, bw);
            bw.close();
        } catch (IOException e) {
            System.out.println("Lỗi lưu file điểm số: " + e.getMessage());
        }
    }

    private void saveTreeToFile(Node node, BufferedWriter bw) throws IOException {
        if (node != null) {
            bw.write(node.name + "," + node.score);
            bw.newLine();
            saveTreeToFile(node.left, bw);
            saveTreeToFile(node.right, bw);
        }
    }
}