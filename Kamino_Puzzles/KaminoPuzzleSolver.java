package Kamino_Puzzles;

import java.util.*;

public class KaminoPuzzleSolver {

    public static class Point {
        public int row, col;

        public Point(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return row == point.row && col == point.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

        @Override
        public String toString() {
            return "(" + row + ", " + col + ")";
        }
    }

    private static class State {
        Point current;
        List<Point> path;

        public State(Point current, List<Point> path) {
            this.current = current;
            this.path = path;
        }
    }

    private String[][] rawGrid;
    private int rows;
    private int cols;
    private Point startPos;
    private Point targetPos;

    public KaminoPuzzleSolver(String[][] rawGrid) {
        this.rawGrid = rawGrid;
        this.rows = rawGrid.length;
        this.cols = rawGrid[0].length;
        locateMarkers();
    }

    private void locateMarkers() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String val = rawGrid[r][c].trim().toUpperCase();
                if (val.equals("S")) {
                    this.startPos = new Point(r, c);
                } else if (val.equals("E") || val.startsWith("E:")) {
                    this.targetPos = new Point(r, c);
                }
            }
        }

        if (startPos == null || targetPos == null) {
            throw new IllegalArgumentException("Grid must contain both a start 'S' and an end 'E' marker.");
        }
    }

    public Point getStartPos() { return startPos; }
    public Point getTargetPos() { return targetPos; }

    public boolean isStartMoreThanHalfway() {
        int distance = Math.abs(startPos.row - targetPos.row) + Math.abs(startPos.col - targetPos.col);
        double halfwayThreshold = (rows + cols) / 2.0;
        return distance > halfwayThreshold;
    }

    private Integer getTileValue(int r, int c) {
        String token = rawGrid[r][c].trim().toUpperCase();
        if (token.equals("S")) return null;
        if (token.startsWith("E:")) {
            return Integer.parseInt(token.substring(2));
        }
        if (token.equals("E")) return 2;
        return Integer.parseInt(token);
    }

    public List<Point> solve() {
        Queue<State> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();

        List<Point> initialPath = new ArrayList<>();
        initialPath.add(startPos);
        queue.add(new State(startPos, initialPath));
        visited.add(startPos);

        int[][] directions = {
            {-1, 0}, // Up
            {1, 0},  // Down
            {0, -1}, // Left
            {0, 1}   // Right
        };
        String[] dirNames = {"up", "down", "left", "right"};

        while (!queue.isEmpty()) {
            State currState = queue.poll();
            Point curr = currState.current;

            if (curr.equals(targetPos)) {
                return currState.path;
            }

            Integer currVal = curr.equals(startPos) ? null : getTileValue(curr.row, curr.col);

            for (int i = 0; i < directions.length; i++) {
                int nr = curr.row + directions[i][0];
                int nc = curr.col + directions[i][1];

                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    Point nextPoint = new Point(nr, nc);

                    // Skip the start marker as a destination tile
                    if (nextPoint.equals(startPos)) {
                        continue;
                    }

                    Integer nextVal = getTileValue(nr, nc);
                    if (nextVal == null) {
                        continue;
                    }

                    boolean validMove = false;

                    // First move out of start marker is unrestricted
                    if (curr.equals(startPos)) {
                        validMove = true;
                    } else if (currVal != null) {
                        String direction = dirNames[i];
                        if (direction.equals("up") && nextVal > currVal) {
                            validMove = true;
                        } else if (direction.equals("down") && nextVal < currVal) {
                            validMove = true;
                        } else if ((direction.equals("left") || direction.equals("right")) && nextVal.equals(currVal)) {
                            validMove = true;
                        }
                    }

                    if (validMove && !visited.contains(nextPoint)) {
                        visited.add(nextPoint);
                        List<Point> newPath = new ArrayList<>(currState.path);
                        newPath.add(nextPoint);
                        queue.add(new State(nextPoint, newPath));
                    }
                }
            }
        }
        return null;
    }
}