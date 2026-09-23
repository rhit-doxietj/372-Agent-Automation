import java.util.ArrayList;
import java.util.List;

public class Recto {

    public static class Rect {
        public int clueId;
        public int r1, c1, r2, c2;

        public Rect(int clueId, int r1, int c1, int r2, int c2) {
            this.clueId = clueId;
            this.r1 = r1;
            this.c1 = c1;
            this.r2 = r2;
            this.c2 = c2;
        }
    }

    private final int[][] grid;
    private final int rows;
    private final int cols;
    private final List<Point> clueLocations = new ArrayList<>();
    private final List<Integer> clueValues = new ArrayList<>();
    private final List<Rect>[] possibleRectsPerClue;
    private final Rect[] solution;
    private final int[][] cellOwner;
    
    private int backtrackCount = 0;
    private static final int MAX_BACKTRACKS = 50000;

    @SuppressWarnings("unchecked")
    public Recto(int[][] grid) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = grid[0].length;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] > 0) {
                    clueLocations.add(new Point(r, c));
                    clueValues.add(grid[r][c]);
                }
            }
        }

        int numClues = clueLocations.size();
        this.possibleRectsPerClue = new List[numClues];
        this.solution = new Rect[numClues];
        this.cellOwner = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cellOwner[i][j] = -1;
            }
        }

        generatePossibleRects();
    }

    private void generatePossibleRects() {
        for (int i = 0; i < clueLocations.size(); i++) {
            possibleRectsPerClue[i] = new ArrayList<>();
            Point clue = clueLocations.get(i);
            int val = clueValues.get(i);

            for (int h = 1; h <= val - 1; h++) {
                int w = val - h;
                if (h > rows || w > cols) continue;

                for (int r1 = Math.max(0, clue.x - h + 1); r1 <= Math.min(rows - h, clue.x); r1++) {
                    int r2 = r1 + h - 1;
                    for (int c1 = Math.max(0, clue.y - w + 1); c1 <= Math.min(cols - w, clue.y); c1++) {
                        int c2 = c1 + w - 1;

                        if (containsOtherClues(i, r1, c1, r2, c2)) continue;
                        possibleRectsPerClue[i].add(new Rect(i, r1, c1, r2, c2));
                    }
                }
            }
        }
    }

    private boolean containsOtherClues(int currentClueId, int r1, int c1, int r2, int c2) {
        for (int i = 0; i < clueLocations.size(); i++) {
            if (i == currentClueId) continue;
            Point clue = clueLocations.get(i);
            if (clue.x >= r1 && clue.x <= r2 && clue.y >= c1 && clue.y <= c2) {
                return true;
            }
        }
        return false;
    }

    public boolean solve() {
        backtrackCount = 0;
        boolean[][] occupied = new boolean[rows][cols];
        return backtrack(0, occupied);
    }

    private boolean backtrack(int clueIndex, boolean[][] occupied) {
        backtrackCount++;
        if (backtrackCount > MAX_BACKTRACKS) return false;

        if (clueIndex == clueLocations.size()) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (!occupied[r][c]) return false;
                }
            }
            return true;
        }

        for (Rect rect : possibleRectsPerClue[clueIndex]) {
            if (canPlace(rect, occupied)) {
                place(rect, occupied, clueIndex);
                solution[clueIndex] = rect;

                if (backtrack(clueIndex + 1, occupied)) {
                    return true;
                }

                remove(rect, occupied);
                solution[clueIndex] = null;
            }
        }
        return false;
    }

    private boolean canPlace(Rect rect, boolean[][] occupied) {
        for (int r = rect.r1; r <= rect.r2; r++) {
            for (int c = rect.c1; c <= rect.c2; c++) {
                if (occupied[r][c]) return false;
            }
        }
        return true;
    }

    private void place(Rect rect, boolean[][] occupied, int clueId) {
        for (int r = rect.r1; r <= rect.r2; r++) {
            for (int c = rect.c1; c <= rect.c2; c++) {
                occupied[r][c] = true;
                cellOwner[r][c] = clueId;
            }
        }
    }

    private void remove(Rect rect, boolean[][] occupied) {
        for (int r = rect.r1; r <= rect.r2; r++) {
            for (int c = rect.c1; c <= rect.c2; c++) {
                occupied[r][c] = false;
                cellOwner[r][c] = -1;
            }
        }
    }

    public Rect getSolutionRect(int clueId) {
        if (clueId >= 0 && clueId < solution.length) {
            return solution[clueId];
        }
        return null;
    }

    public int getCellOwner(int r, int c) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            return cellOwner[r][c];
        }
        return -1;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getBacktrackCount() { return backtrackCount; }
    public String getDifficultyWithDimensions() {
        return rows + "x" + cols + " Grid";
    }

    public static class Point {
        public int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}