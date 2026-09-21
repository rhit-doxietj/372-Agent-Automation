import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RectoGenerator {

    static class Box {
        int r1, c1, r2, c2;

        Box(int r1, int c1, int r2, int c2) {
            this.r1 = r1;
            this.c1 = c1;
            this.r2 = r2;
            this.c2 = c2;
        }

        int height() { return r2 - r1 + 1; }
        int width()  { return c2 - c1 + 1; }
    }

    private static final Random rand = new Random();

    public static int[][] generateUnique(int rows, int cols) {
        int maxAttempts = 500;
        for (int i = 0; i < maxAttempts; i++) {
            int[][] grid = generate(rows, cols);
            Recto solver = new Recto(grid);
            if (solver.countSolutions(2) == 1) {
                return grid;
            }
        }
        return generate(rows, cols);
    }

    public static int[][] generate(int rows, int cols) {
        int minSide = Math.max(1, Math.min(rows, cols) / 4);
        int maxSide = Math.max(2, Math.min(rows, cols) / 2 + 1);

        int maxTwos = Math.max(1, (rows * cols) / 16);
        int currentTwos = 0;

        List<Box> partitions = new ArrayList<>();
        partition(new Box(0, 0, rows - 1, cols - 1), minSide, maxSide, partitions);

        int[][] puzzle = new int[rows][cols];

        for (Box box : partitions) {
            int h = box.height();
            int w = box.width();
            int clueValue = h + w;

            if (clueValue == 2) {
                if (currentTwos >= maxTwos) {
                    continue; 
                }
                currentTwos++;
            }

            int clueR = box.r1 + rand.nextInt(h);
            int clueC = box.c1 + rand.nextInt(w);

            puzzle[clueR][clueC] = clueValue;
        }

        return puzzle;
    }

    private static void partition(Box box, int minSide, int maxSide, List<Box> result) {
        int h = box.height();
        int w = box.width();

        boolean canSplitH = h >= 2 * minSide;
        boolean canSplitV = w >= 2 * minSide;

        boolean mustSplit = (h > maxSide) || (w > maxSide);
        if (!mustSplit && (!canSplitH && !canSplitV || rand.nextDouble() < 0.25)) {
            result.add(box);
            return;
        }

        if (!canSplitH && !canSplitV) {
            result.add(box);
            return;
        }

        boolean splitHorizontal;
        if (canSplitH && canSplitV) {
            if (h > w * 1.5) {
                splitHorizontal = true;
            } else if (w > h * 1.5) {
                splitHorizontal = false;
            } else {
                splitHorizontal = rand.nextBoolean();
            }
        } else {
            splitHorizontal = canSplitH;
        }

        if (splitHorizontal) {
            int splitR = box.r1 + minSide + rand.nextInt(h - 2 * minSide + 1) - 1;
            partition(new Box(box.r1, box.c1, splitR, box.c2), minSide, maxSide, result);
            partition(new Box(splitR + 1, box.c1, box.r2, box.c2), minSide, maxSide, result);
        } else {
            int splitC = box.c1 + minSide + rand.nextInt(w - 2 * minSide + 1) - 1;
            partition(new Box(box.r1, box.c1, box.r2, splitC), minSide, maxSide, result);
            partition(new Box(box.r1, splitC + 1, box.r2, box.c2), minSide, maxSide, result);
        }
    }
}