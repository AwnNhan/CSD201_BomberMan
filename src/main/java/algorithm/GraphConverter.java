/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package algorithm;

import java.util.ArrayList;
import java.util.List;

public class GraphConverter {

    private List<List<Integer>> adjacencyList;
    private final int maxRow = 13;
    private final int maxCol = 15;
    private final int totalVertices;

    public GraphConverter() {
        this.totalVertices = maxRow * maxCol;
        this.adjacencyList = new ArrayList<>(totalVertices);

        for (int i = 0; i < totalVertices; i++) {
            this.adjacencyList.add(new ArrayList<>());
        }
    }

    public void updateGraph(int[][] mapMatrix) {
        for (int i = 0; i < totalVertices; i++) {
            adjacencyList.get(i).clear();
        }

        int[] rowDir = {-1, 1, 0, 0};
        int[] colDir = {0, 0, -1, 1};

        for (int r = 0; r < maxRow; r++) {
            for (int c = 0; c < maxCol; c++) {

                if (mapMatrix[r][c] == 0) {
                    int currentId = r * maxCol + c;

                    for (int i = 0; i < 4; i++) {
                        int nextR = r + rowDir[i];
                        int nextC = c + colDir[i];

                        if (nextR >= 0 && nextR < maxRow && nextC >= 0 && nextC < maxCol) {
                            if (mapMatrix[nextR][nextC] == 0) {
                                int neighborId = nextR * maxCol + nextC;
                                adjacencyList.get(currentId).add(neighborId);
                            }
                        }
                    }
                }
            }
        }
    }

    public List<List<Integer>> getGraph() {
        return adjacencyList;
    }
}
