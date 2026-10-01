package Kamino_Puzzles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KaminoPuzzleGenerator {

    private static final Random random = new Random();

    /**
     * Generates a square solvable Kamino puzzle of dimension size x size (up to 75).
     */
    public static String[][] generate(int size) {
        return generate(size, size, true);
    }

    /**
     * Generates a square Kamino puzzle with specified solvability.
     */
    public static String[][] generate(int size, boolean solvable) {
        return generate(size, size, solvable);
    }

    /**
     * Generates a rectangular puzzle of dimension rows x cols with specified solvability.
     * @param rows number of rows (1 to 75)
     * @param cols number of columns (1 to 75)
     * @param solvable true to generate a board with a guaranteed path; false for an unsolvable board
     */
    public static String[][] generate(int rows, int cols, boolean solvable) {
        if (rows * cols < 2) {
            throw new IllegalArgumentException("Grid must have at least 2 cells to place both Start ('S') and End ('E') markers.");
        }

        if (solvable) {
            return generateSolvablePuzzle(rows, cols);
        } else {
            return generateUnsolvablePuzzle(rows, cols);
        }
    }

    private static String[][] generateSolvablePuzzle(int rows, int cols) {
        double threshold = (rows + cols) / 2.0;
        int sr = 0, sc = cols - 1;
        int er = rows - 1, ec = 0;
        boolean foundDistance = false;

        // Try picking random S and E that satisfy the halfway distance rule
        for (int i = 0; i < 500; i++) {
            int r1 = random.nextInt(rows);
            int c1 = random.nextInt(cols);
            int r2 = random.nextInt(rows);
            int c2 = random.nextInt(cols);
            int dist = Math.abs(r1 - r2) + Math.abs(c1 - c2);

            if ((rows + cols > 4 && dist > threshold) || (rows + cols <= 4 && (r1 != r2 || c1 != c2))) {
                sr = r1;
                sc = c1;
                er = r2;
                ec = c2;
                foundDistance = true;
                break;
            }
        }

        // Fallback to opposite corners if random sampling doesn't hit threshold
        if (!foundDistance) {
            sr = 0;
            sc = cols - 1;
            er = rows - 1;
            ec = 0;
        }

        // 1. Carve a randomized path from Start to Exit
        List<KaminoPuzzleSolver.Point> path = new ArrayList<>();
        int currR = sr;
        int currC = sc;
        path.add(new KaminoPuzzleSolver.Point(currR, currC));

        while (currR != er || currC != ec) {
            List<int[]> choices = new ArrayList<>();
            if (currR < er) choices.add(new int[]{1, 0});  // down
            else if (currR > er) choices.add(new int[]{-1, 0}); // up

            if (currC < ec) choices.add(new int[]{0, 1});  // right
            else if (currC > ec) choices.add(new int[]{0, -1}); // left

            int[] move = choices.get(random.nextInt(choices.size()));
            currR += move[0];
            currC += move[1];
            path.add(new KaminoPuzzleSolver.Point(currR, currC));
        }

        // 2. Assign relative step values along the path matching Kamino rules
        int[] relVals = new int[path.size()];
        int minRel = 0;
        for (int i = 1; i < path.size(); i++) {
            KaminoPuzzleSolver.Point prev = path.get(i - 1);
            KaminoPuzzleSolver.Point curr = path.get(i);

            if (curr.row < prev.row) { // UP -> next must be strictly higher
                relVals[i] = relVals[i - 1] + 1;
            } else if (curr.row > prev.row) { // DOWN -> next must be strictly lower
                relVals[i] = relVals[i - 1] - 1;
            } else { // LEFT or RIGHT -> must be equal
                relVals[i] = relVals[i - 1];
            }

            if (i == 1 || relVals[i] < minRel) {
                minRel = relVals[i];
            }
        }

        // 3. Shift relative values so the minimum tile value is at least 1
        int shift = 1 - minRel;
        int[] finalVals = new int[path.size()];
        int maxVal = 9;
        for (int i = 1; i < path.size(); i++) {
            finalVals[i] = relVals[i] + shift;
            if (finalVals[i] > maxVal) {
                maxVal = finalVals[i];
            }
        }

        // 4. Fill background grid with random numbers
        String[][] grid = new String[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = String.valueOf(1 + random.nextInt(maxVal));
            }
        }

        // 5. Stamp the valid path onto the board
        for (int i = 1; i < path.size() - 1; i++) {
            KaminoPuzzleSolver.Point p = path.get(i);
            grid[p.row][p.col] = String.valueOf(finalVals[i]);
        }

        // Place S and E
        grid[sr][sc] = "S";
        KaminoPuzzleSolver.Point endPoint = path.get(path.size() - 1);
        grid[endPoint.row][endPoint.col] = "E:" + finalVals[path.size() - 1];

        return grid;
    }

    private static String[][] generateUnsolvablePuzzle(int rows, int cols) {
        String[][] grid = new String[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = String.valueOf(1 + random.nextInt(9));
            }
        }

        int sr = 0, sc = cols - 1;
        int er = rows - 1, ec = 0;
        grid[sr][sc] = "S";
        grid[er][ec] = "E:9";

        // Surround target E:9 with 1s:
        // - From above: downward step requires lower, but 9 < 1 is false.
        // - From right: horizontal step requires equality, but 1 == 9 is false.
        int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] offset : neighbors) {
            int nr = er + offset[0];
            int nc = ec + offset[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !(nr == sr && nc == sc)) {
                grid[nr][nc] = "1";
            }
        }

        return grid;
    }
}