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
    private final int maxBacktracks;

    @SuppressWarnings("unchecked")
    public Recto(int[][] grid) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = grid[0].length;
        
        this.maxBacktracks = Math.max(50000, rows * cols * 50);

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

    /**
     * Maps ownership of EVERY cell within a solution rectangle.
     * Guarantees getCellOwner(r, c) returns a valid non-negative ID for inner boundary checks.
     */
    public void setSolutionRect(int clueId, Rect rect) {
        if (rect == null) return;
        
        if (clueId >= 0 && clueId < solution.length) {
            solution[clueId] = rect;
        }
        
        for (int r = rect.r1; r <= rect.r2; r++) {
            for (int c = rect.c1; c <= rect.c2; c++) {
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    cellOwner[r][c] = clueId;
                }
            }
        }
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
        if (backtrackCount > maxBacktracks) return false;

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

    public int countSolutions(int limit) {
        backtrackCount = 0;
        boolean[][] occupied = new boolean[rows][cols];
        return countSolutionsBacktrack(0, occupied, 0, limit);
    }

    private int countSolutionsBacktrack(int clueIndex, boolean[][] occupied, int currentCount, int limit) {
        backtrackCount++;
        if (backtrackCount > maxBacktracks || currentCount >= limit) return currentCount;

        if (clueIndex == clueLocations.size()) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (!occupied[r][c]) return currentCount;
                }
            }
            return currentCount + 1;
        }

        for (Rect rect : possibleRectsPerClue[clueIndex]) {
            if (canPlace(rect, occupied)) {
                place(rect, occupied, clueIndex);
                currentCount = countSolutionsBacktrack(clueIndex + 1, occupied, currentCount, limit);
                remove(rect, occupied);

                if (currentCount >= limit) break;
            }
        }
        return currentCount;
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

    public static class Point {
        public int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    
}