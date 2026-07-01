/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author ADMIN
 */
public class PathFinder {
    private static final int[] rowDir = {-1, 1, 0, 0}; // Lên, Xuống
    private static final int[] colDir = {0, 0, -1, 1}; // Trái, Phải

    public List<GridPoint> bfsSearch(GridPoint start, GridPoint target, int[][] map) {
        int rows = map.length;
        int cols = map[0].length;
        
        boolean[][] visited = new boolean[rows][cols];
        GridPoint[][] trace = new GridPoint[rows][cols];
        Queue<GridPoint> queue = new LinkedList<>();

        queue.add(start);
        visited[start.r][start.c] = true;
        boolean found = false;

        while (!queue.isEmpty()) {
            GridPoint curr = queue.poll();

            if (curr.r == target.r && curr.c == target.c) {
                found = true;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int nextR = curr.r + rowDir[i];
                int nextC = curr.c + colDir[i];

                // LUẬT: Nằm trong bản đồ, chưa đi qua, và là ô trống (ví dụ map == 0)
                if (nextR >= 0 && nextR < rows && nextC >= 0 && nextC < cols) {
                    if (!visited[nextR][nextC] && map[nextR][nextC] == 0) {
                        visited[nextR][nextC] = true;
                        queue.add(new GridPoint(nextR, nextC));
                        trace[nextR][nextC] = curr;
                    }
                }
            }
        }

        List<GridPoint> path = new ArrayList<>();
        if (found) {
            GridPoint step = target;
            while (step.r != start.r || step.c != start.c) {
                path.add(step);
                step = trace[step.r][step.c];
            }
            Collections.reverse(path);
        }
        return path;
    }
}
