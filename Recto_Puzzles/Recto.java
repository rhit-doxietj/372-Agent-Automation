import java.util.*;

public class Recto {

    static class Clue {
        int id;
        int r, c;
        int sum;

        Clue(int id, int r, int c, int sum) {
            this.id = id;
            this.r = r;
            this.c = c;
            this.sum = sum;
        }
    }

    static class Rect {
        int clueId;
        int r1, c1, r2, c2;

        Rect(int clueId, int r1, int c1, int r2, int c2) {
            this.clueId = clueId;
            this.r1 = r1;
            this.c1 = c1;
            this.r2 = r2;
            this.c2 = c2;
        }
    }

    private final int rows;
    private final int cols;
    private final int[][] grid;
    private final List<Clue> clues = new ArrayList<>();
    private final List<Rect>[][] cellCandidates;
    private final int[][] cellOwner;
    private final boolean[] cluePlaced;

    // Difficulty tracking fields
    private int backtrackCount = 0;
    private boolean isSolved = false;

    @SuppressWarnings("unchecked")
    public Recto(int[][] grid) {
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.grid = grid;
        this.cellOwner = new int[rows][cols];
        for (int[] row : cellOwner) {
            Arrays.fill(row, -1);
        }

        int id = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] > 0) {
                    clues.add(new Clue(id++, r, c, grid[r][c]));
                }
            }
        }
        this.cluePlaced = new boolean[clues.size()];
        this.cellCandidates = new ArrayList[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cellCandidates[r][c] = new ArrayList<>();
            }
        }

        generateCandidates();
    }

    private void generateCandidates() {
        for (Clue clue : clues) {
            for (int h = 1; h < clue.sum; h++) {
                int w = clue.sum - h;
                if (h > rows || w > cols) continue;

                int minR = Math.max(0, clue.r - h + 1);
                int maxR = Math.min(rows - h, clue.r);
                int minC = Math.max(0, clue.c - w + 1);
                int maxC = Math.min(cols - w, clue.c);

                for (int r1 = minR; r1 <= maxR; r1++) {
                    for (int c1 = minC; c1 <= maxC; c1++) {
                        int r2 = r1 + h - 1;
                        int c2 = c1 + w - 1;

                        if (!containsOtherClues(r1, c1, r2, c2, clue.id)) {
                            Rect rect = new Rect(clue.id, r1, c1, r2, c2);
                            for (int r = r1; r <= r2; r++) {
                                for (int c = c1; c <= c2; c++) {
                                    cellCandidates[r][c].add(rect);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean containsOtherClues(int r1, int c1, int r2, int c2, int currentId) {
        for (Clue other : clues) {
            if (other.id == currentId) continue;
            if (other.r >= r1 && other.r <= r2 && other.c >= c1 && other.c <= c2) {
                return true;
            }
        }
        return false;
    }

    public boolean solve() {
        backtrackCount = 0;
        isSolved = solveExactCover();
        return isSolved;
    }

    private boolean solveExactCover() {
        int bestR = -1;
        int bestC = -1;
        int minChoices = Integer.MAX_VALUE;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cellOwner[r][c] == -1) {
                    int validCount = 0;
                    for (Rect rect : cellCandidates[r][c]) {
                        if (!cluePlaced[rect.clueId] && canPlace(rect)) {
                            validCount++;
                        }
                    }

                    if (validCount == 0) {
                        return false;
                    }

                    if (validCount < minChoices) {
                        minChoices = validCount;
                        bestR = r;
                        bestC = c;
                        if (minChoices == 1) break;
                    }
                }
            }
            if (minChoices == 1) break;
        }

        if (bestR == -1) {
            for (boolean placed : cluePlaced) {
                if (!placed) return false;
            }
            return true;
        }

        for (Rect rect : cellCandidates[bestR][bestC]) {
            if (!cluePlaced[rect.clueId] && canPlace(rect)) {
                place(rect);

                if (solveExactCover()) {
                    return true;
                }

                unplace(rect);
                backtrackCount++; // Branch failed; track backtrack step
            }
        }

        return false;
    }

    private boolean canPlace(Rect rect) {
        for (int r = rect.r1; r <= rect.r2; r++) {
            for (int c = rect.c1; c <= rect.c2; c++) {
                if (cellOwner[r][c] != -1) return false;
            }
        }
        return true;
    }

    private void place(Rect rect) {
        cluePlaced[rect.clueId] = true;
        for (int r = rect.r1; r <= rect.r2; r++) {
            for (int c = rect.c1; c <= rect.c2; c++) {
                cellOwner[r][c] = rect.clueId;
            }
        }
    }

    private void unplace(Rect rect) {
        cluePlaced[rect.clueId] = false;
        for (int r = rect.r1; r <= rect.r2; r++) {
            for (int c = rect.c1; c <= rect.c2; c++) {
                cellOwner[r][c] = -1;
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getBacktrackCount() {
        return backtrackCount;
    }

    public String getDifficulty() {
        if (!isSolved) {
            return "Unsolvable";
        }
        if (backtrackCount == 0) {
            return "Easy";
        } else if (backtrackCount <= 10) {
            return "Medium";
        } else if (backtrackCount <= 50) {
            return "Hard";
        } else {
            return "Expert";
        }
    }

    public String getDifficultyWithDimensions() {
        return getDifficulty() + " " + rows + "x" + cols;
    }

    public void printSolution() {
        for (int r = 0; r <= rows; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                sb.append("+");
                boolean border = (r == 0 || r == rows || cellOwner[r - 1][c] != cellOwner[r][c]);
                sb.append(border ? "---" : "   ");
            }
            sb.append("+");
            System.out.println(sb);

            if (r < rows) {
                StringBuilder rowStr = new StringBuilder();
                for (int c = 0; c < cols; c++) {
                    boolean vBorder = (c == 0 || cellOwner[r][c - 1] != cellOwner[r][c]);
                    rowStr.append(vBorder ? "|" : " ");

                    if (grid[r][c] > 0) {
                        rowStr.append(String.format(" %d ", grid[r][c]));
                    } else {
                        rowStr.append(" . ");
                    }
                }
                rowStr.append("|");
                System.out.println(rowStr);
            }
        }
    }

    public static void main(String[] args) {
        int[][] puzzle = {
            {4, 6, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 3, 5, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 3, 2, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 5, 4, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 7, 6, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 6, 7}
        };

        Recto solver = new Recto(puzzle);
        if (solver.solve()) {
            System.out.println("Rating: " + solver.getDifficultyWithDimensions() 
                + " (Backtracks: " + solver.getBacktrackCount() + ")");
            solver.printSolution();
        } else {
            System.out.println("No solution found.");
        }
    }
}